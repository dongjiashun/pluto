package net.pluto.backup.store;

import net.pluto.backup.Backup;
import net.pluto.backup.BackupCodec;
import net.pluto.backup.BackupEnvironment;
import net.pluto.util.BackupUtil;
import net.pluto.util.Configuration;
import net.pluto.util.Constants;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryBackupStore extends AbstractBackupStore {

    private static final LinkedList<Backup> backups = new LinkedList<>();

    private static final ConcurrentHashMap<Long, Backup> backupStore = new ConcurrentHashMap<>();

    private static final Random idGenerator = new Random();

    private static final BackupCodec codec = BackupCodec.JSON;

    public InMemoryBackupStore(BackupStore persistBackupStore) {
        loadBackupData(persistBackupStore);
    }

    private void loadBackupData(BackupStore persistBackupStore) {
        List<Backup> persistedBackups = persistBackupStore.getBackups();
        if (!CollectionUtils.isEmpty(persistedBackups)) {
            for (Backup backup : persistedBackups) {
                File file = new File(backup.getBackupDirectory());
                if (!file.exists()) {
                    throw new IllegalStateException("backup file doesn't exist, file " + backup.getBackupDirectory());
                }
                storeBackup(backup);
            }
            Backup latestBackup = backups.peekLast();
            // 备份文件(写入日志失败)
            BackupEnvironment environment = Configuration.getInstance().getBackupEnvironment(latestBackup.getInstance());
            File backupDir = new File(Configuration.getInstance().getProperty(Constants.BACKUP_DIR_KEY));
            File latestBackupFile = BackupUtil.getLatestXtrabackupDirectory(backupDir);
            assert latestBackupFile != null;
            if (!latestBackup.getBackupDirectory().equals(latestBackupFile.getAbsolutePath())) {
                try {
                    File xtrabackupLogInfo = new File(BackupUtil.getXtrabackupLogInfoFilePath(latestBackupFile, environment));
                    if (xtrabackupLogInfo.exists()) {
                        Backup backup = codec.readBackup(FileUtils.readFileToByteArray(xtrabackupLogInfo));
                        if (backup.compareTo(latestBackup) < 0) {
                            throw new IllegalStateException("backup: " + backup + " is stale but not removed");
                        }
                        storeBackup(backup);
                        persistBackupStore.save(backups);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Override
    public synchronized long append(Backup backup) {
        long newId = 0L;
        BackupEnvironment environment = Configuration.getInstance().getBackupEnvironment(backup.getInstance());
        File backupDir = new File(Configuration.getInstance().getProperty(Constants.BACKUP_DIR_KEY));
        File latestBackup = BackupUtil.getLatestXtrabackupDirectory(backupDir);
        if (latestBackup == null) {
            throw new IllegalStateException("latestBackup dir must not be null");
        }
        long backupSize;
        if (latestBackup.isDirectory()) {
            backupSize = FileUtils.sizeOfDirectory(latestBackup);
        } else {
            backupSize = latestBackup.length();
        }
        Backup.Builder builder = null;
        if (CollectionUtils.isEmpty(backups)) {
            long traceId = generateNewId();
            newId = traceId;
            builder = backup.newBuilder();
            builder
                    .traceId(traceId)
                    .parentId(traceId)
                    .id(newId)
                    .backupSize(backupSize / FileUtils.ONE_MB)
                    .backupDirectory(latestBackup.getAbsolutePath());
        } else {
            // poll last
            Backup lastBackup = backups.pollLast();
            long traceId = lastBackup.getTraceId();
            long lastId = lastBackup.getId();
            newId = generateNewId();
            // update last again
            builder = lastBackup.newBuilder();
            lastBackup = builder.childId(newId).build();
            storeBackup(lastBackup);

            // add new backup
            builder = backup.newBuilder();
            builder
                    .traceId(traceId)
                    .id(newId)
                    .parentId(lastId)
                    .backupSize(backupSize / FileUtils.ONE_MB)
                    .backupDirectory(latestBackup.getAbsolutePath());
        }
        if (backup.getTimestamp() == null) {
            builder.timestamp(System.currentTimeMillis());
        }
        Backup newBackup = builder.build();
        storeBackup(newBackup);
        recordXtrabackupLogInfo(backup);
        return newId;
    }

    /**
     * create file xtrabackup_log_info record backup
     *
     * @param backup backup
     */
    private void recordXtrabackupLogInfo(Backup backup) {
        byte[] bytes = codec.writeBackup(backup);
        BackupEnvironment environment = Configuration.getInstance().getBackupEnvironment(backup.getInstance());
        File backupDir = new File(Configuration.getInstance().getProperty(Constants.BACKUP_DIR_KEY));
        File latestBackupDir = BackupUtil.getLatestXtrabackupDirectory(backupDir);
        File xtrabackupLogInfo = new File(BackupUtil.getXtrabackupLogInfoFilePath(latestBackupDir, environment));
        try {
            FileUtils.writeByteArrayToFile(xtrabackupLogInfo, bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void storeBackup(Backup backup) {
        backups.add(backup);
        backupStore.put(backup.getId(), backup);
    }

    private Long generateNewId() {
        return idGenerator.nextLong();
    }

    @Override
    public void save(List<Backup> backups) {
        // NOP
    }

    @Override
    public synchronized List<Backup> getBackups() {
        return backups;
    }

    @Override
    public Backup getBackup(long id) {
        return backupStore.get(id);
    }

    @Override
    public void removeBackup(Backup backup) {
        backups.remove(backup);
        backupStore.remove(backup.getId());
    }
}
