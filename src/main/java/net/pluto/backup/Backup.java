package net.pluto.backup;

import java.io.Serializable;
import java.util.List;

public class Backup implements Comparable<Backup>, Serializable {

    private final Long traceId;

    private final Long id;

    private final Long parentId;

    private final Long childId;

    private final String name;

    private final Long timestamp;

    private final Long duration;

    private final Long backupSize;

    private final BackupType backupType;

    private final String instance;

    private final String backupDirectory;

    private final List<String> databases;

    private Backup(Builder builder) {
        this.traceId = builder.traceId;
        this.id = builder.id;
        this.parentId = builder.parentId;
        this.childId = builder.childId;
        this.name = builder.name;
        this.timestamp = builder.timestamp;
        this.duration = builder.duration;
        this.backupSize = builder.backupSize;
        this.backupType = builder.backupType;
        this.instance = builder.instance;
        this.backupDirectory = builder.backupDirectory;
        this.databases = builder.databases;
    }

    public Long getTraceId() {
        return traceId;
    }

    public Long getId() {
        return id;
    }

    public Long getParentId() {
        return parentId;
    }

    public String getName() {
        return name;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public Long getChildId() {
        return childId;
    }

    public Long getDuration() {
        return duration;
    }

    public Long getBackupSize() {
        return backupSize;
    }

    public BackupType getBackupType() {
        return backupType;
    }

    public String getInstance() {
        return instance;
    }

    public String getBackupDirectory() {
        return backupDirectory;
    }

    public List<String> getDatabases() {
        return databases;
    }

    public Builder newBuilder() {
        return new Builder(this);
    }

    public static class Builder {
        private Long traceId;
        private Long id;
        private Long parentId;
        private Long childId;
        private String name;
        private Long timestamp;
        private Long duration;
        private Long backupSize;
        private BackupType backupType;
        private String backupDirectory;
        private String instance;
        private List<String> databases;

        public Builder() {
        }

        public Builder(Backup backup) {
            this.traceId = backup.traceId;
            this.id = backup.id;
            this.parentId = backup.parentId;
            this.childId = backup.childId;
            this.name = backup.name;
            this.timestamp = backup.timestamp;
            this.duration = backup.duration;
            this.backupSize = backup.backupSize;
            this.backupType = backup.backupType;
            this.instance = backup.instance;
            this.backupDirectory = backup.backupDirectory;
            this.databases = backup.databases;
        }

        public Builder traceId(Long traceId) {
            this.traceId = traceId;
            return this;
        }

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder parentId(Long parentId) {
            this.parentId = parentId;
            return this;
        }

        public Builder childId(Long childId) {
            this.childId = childId;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder timestamp(Long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder duration(Long duration) {
            this.duration = duration;
            return this;
        }

        public Builder backupSize(Long backupSize) {
            this.backupSize = backupSize;
            return this;
        }

        public Builder backupType(BackupType backupType) {
            this.backupType = backupType;
            return this;
        }

        public Builder instance(String instance) {
            this.instance = instance;
            return this;
        }

        public Builder backupDirectory(String backupDirectory) {
            this.backupDirectory = backupDirectory;
            return this;
        }

        public Builder databases(List<String> databases) {
            this.databases = databases;
            return this;
        }

        public Backup build() {
            return new Backup(this);
        }
    }

    @Override
    public int compareTo(Backup o) {
        return timestamp.compareTo(o.timestamp);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Backup backup = (Backup) o;

        if (!id.equals(backup.id)) return false;
        if (!name.equals(backup.name)) return false;
        return timestamp.equals(backup.timestamp);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + timestamp.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Backup{" +
                "traceId=" + traceId +
                ", id=" + id +
                ", parentId=" + parentId +
                ", childId=" + childId +
                ", name='" + name + '\'' +
                ", timestamp=" + timestamp +
                ", duration=" + duration +
                ", backupSize=" + backupSize +
                ", backupType=" + backupType +
                ", instance='" + instance + '\'' +
                ", backupDirectory='" + backupDirectory + '\'' +
                ", databases=" + databases +
                '}';
    }
}
