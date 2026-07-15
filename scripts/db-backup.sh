#!/bin/bash

# ==============================================================================
# STARTUP CEO SIMULATOR - PRODUCTION RELATIONAL DATABASE BACKUP AUTOMATION
# ==============================================================================
# This script dumps the active PostgreSQL database, compresses the output,
# encrypts it with a symmetric GPG key, and uploads it to secure cloud storage.
#
# Best Practice: Run this script via a daily Cron job in the production system.
# Crontab entry example (runs daily at 2:00 AM):
# 0 2 * * * /scripts/db-backup.sh >> /var/log/db-backup.log 2>&1
# ==============================================================================

set -o pipefail

# Configuration
BACKUP_DIR="/var/backups/startup_ceo"
DB_CONTAINER_NAME="startup-ceo-database"
DB_NAME="startup_ceo_prod"
DB_USER="startup_ceo_user"
RETENTION_DAYS=30
TIMESTAMP=$(date +%Y%m%d%H%M%S)
BACKUP_FILENAME="${DB_NAME}_backup_${TIMESTAMP}.sql"
COMPRESSED_FILENAME="${BACKUP_FILENAME}.gz"
ENCRYPTED_FILENAME="${COMPRESSED_FILENAME}.gpg"

# Logging helpers
log_info() { echo "[$(date --rfc-3339=seconds)] [INFO] $1"; }
log_warn() { echo "[$(date --rfc-3339=seconds)] [WARN] $1" >&2; }
log_error() { echo "[$(date --rfc-3339=seconds)] [ERROR] $1" >&2; }

# Step 1: Create local directory if missing
mkdir -p "$BACKUP_DIR"
chmod 700 "$BACKUP_DIR"

log_info "Initiating database backup pipeline for db: '$DB_NAME'..."

# Step 2: Execute pg_dump inside active PostgreSQL container
if ! docker exec "$DB_CONTAINER_NAME" pg_dump -U "$DB_USER" -d "$DB_NAME" > "${BACKUP_DIR}/${BACKUP_FILENAME}"; then
    log_error "Critical: pg_dump execution failed! Check container status or database parameters."
    exit 1
fi
log_info "Relational schema and transaction state dumped successfully."

# Step 3: GZIP Compression
if ! gzip -f "${BACKUP_DIR}/${BACKUP_FILENAME}"; then
    log_error "Compression of dump file failed."
    exit 1
fi
log_info "Backup dump compressed to .gz format."

# Step 4: GPG Symmetric Encryption (Guard private player data)
if [ -n "$BACKUP_ENCRYPTION_PASSPHRASE" ]; then
    log_info "Encrypting backup archive with GPG..."
    if ! echo "$BACKUP_ENCRYPTION_PASSPHRASE" | gpg --batch --yes --passphrase-fd 0 \
         --symmetric --cipher-algo AES256 \
         -o "${BACKUP_DIR}/${ENCRYPTED_FILENAME}" "${BACKUP_DIR}/${COMPRESSED_FILENAME}"; then
        log_error "GPG encryption failed."
        exit 1
    fi
    # Remove unencrypted zip
    rm -f "${BACKUP_DIR}/${COMPRESSED_FILENAME}"
    FINAL_BACKUP_FILE="${BACKUP_DIR}/${ENCRYPTED_FILENAME}"
    log_info "Symmetric AES256 encryption complete."
else
    log_warn "BACKUP_ENCRYPTION_PASSPHRASE is not set. Backup is saved UNENCRYPTED."
    FINAL_BACKUP_FILE="${BACKUP_DIR}/${COMPRESSED_FILENAME}"
fi

# Step 5: Optional upload to AWS S3 Cloud Storage
if [ -n "$AWS_S3_BACKUP_BUCKET" ]; then
    log_info "Uploading backup archive to AWS S3: s3://${AWS_S3_BACKUP_BUCKET}..."
    if aws s3 cp "$FINAL_BACKUP_FILE" "s3://${AWS_S3_BACKUP_BUCKET}/backups/$(basename "$FINAL_BACKUP_FILE")"; then
        log_info "Backup successfully archived in AWS S3 Glacier storage."
    else
        log_warn "Failed to upload backup archive to AWS S3."
    fi
fi

# Step 6: Enforce retention policies and clean old files
log_info "Cleaning backups older than $RETENTION_DAYS days in $BACKUP_DIR..."
find "$BACKUP_DIR" -type f -name "${DB_NAME}_backup_*" -mtime +$RETENTION_DAYS -delete
log_info "Retention cleanup completed successfully."

log_info "Backup pipeline concluded successfully. Secure Archive: $FINAL_BACKUP_FILE"
exit 0
