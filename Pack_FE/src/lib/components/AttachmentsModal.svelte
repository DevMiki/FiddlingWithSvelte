<script lang="ts">
    import {t} from 'svelte-i18n';
    import {onDestroy, onMount} from 'svelte';
    import type {AttachmentMetadata} from '$lib/types/resource';
    import {showToast} from '$lib/stores/toast';
    import {downloadAttachmentFile, getAttachmentViewUrl, getResourceAttachments} from '$lib/web/resource-service';

    export let resourceId: number;

    export let onCloseDialog: () => void;

    let attachments: AttachmentMetadata[] = [];
    let isLoadingAttachments = true;
    let loadingErrorMessage: string | null = null;
    let attachmentsModal: HTMLDivElement;

    onMount(async () => {
        attachmentsModal?.focus();
        try {
            attachments = await getResourceAttachments(resourceId);
        } catch (error: any) {
            loadingErrorMessage = error.message || $t('attachmentsModal.errorLoading');
            // Added fallback for toast
            showToast(loadingErrorMessage ?? "Error loading attachments", 'error');
        } finally {
            isLoadingAttachments = false;
        }
        // This is called for every key press
        window.addEventListener('keydown', handleGlobalKeyDown);
    });

    onDestroy(() => {
        window.removeEventListener('keydown', handleGlobalKeyDown);
    });

    function closeDialog() {
        if (onCloseDialog) {
            onCloseDialog();
        }
    }

    // Just to close the dialog by clicking outside (on the overlay)
    // event.target: This property always refers to the innermost element that was the actual target of the event.
    // If the user clicks directly on a button inside modal-content-container, then event.target would be that button
    // event.currentTarget: This property always refers to the element
    // to which the event listener is currently attached and for which the event is being processed.
    function handleOverlayClick(event: MouseEvent) {
        if (event.target === event.currentTarget) {
            closeDialog();
        }
    }

    // To remove this error that was bothering me: "Svelte: Visible, non-interactive elements with a click event
    // must be accompanied by a keyboard event handler."
    function handleOverlayKeyDown(event: KeyboardEvent) {
        if (event.target === event.currentTarget && (event.key === 'Enter' || event.key === ' ')) {
            closeDialog();
        }
    }

    function handleGlobalKeyDown(event: KeyboardEvent) {
        if (event.key === 'Escape') {
            closeDialog();
        }
    }

    function formatDisplayFileSize(bytes: number, decimals = 2): string {
        if (bytes === 0) return '0 Bytes';
        const k = 1024;
        const dm = decimals < 0 ? 0 : decimals;
        const units = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return `${parseFloat((bytes / Math.pow(k, i)).toFixed(dm))} ${units[i]}`;
    }

    async function handleDownloadClick(attachment: AttachmentMetadata) {
        try {
            await downloadAttachmentFile(attachment.id, attachment.fileName);
            showToast($t('attachmentsModal.downloadStarted', { values: { file: attachment.fileName } }), 'success');
        } catch (error: any) {
            showToast($t('attachmentsModal.downloadError', { values: { file: attachment.fileName, error: error.message } }), 'error');
        }
    }

    function handleViewClick(attachment: AttachmentMetadata) {
        window.open(getAttachmentViewUrl(attachment.id), '_blank');
    }

    function getFilePreviewType(fileType: string | null | undefined): 'image' | 'icon' {
        if (fileType && fileType.startsWith('image/')) return 'image';
        return 'icon';
    }

    function getIconClassForFileType(fileType: string | null | undefined): string {
        // Default if fileType is missing
        if (!fileType) return 'bi-file-earmark';
        if (fileType === 'application/pdf') return 'bi-file-earmark-pdf';
        if (fileType.startsWith('audio/mpeg')) return 'bi-file-earmark-music';
        if (fileType.startsWith('video/')) return 'bi-file-earmark-play';
        if (fileType.startsWith('text/')) return 'bi-file-earmark-text';
        if (fileType.includes('word') || fileType === 'application/msword' || fileType === 'application/vnd.openxmlformats-officedocument.wordprocessingml.document') return 'bi-file-earmark-word';
        if (fileType.includes('excel') || fileType === 'application/vnd.ms-excel' || fileType === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet') return 'bi-file-earmark-excel';
        if (fileType.includes('presentation') || fileType === 'application/vnd.ms-powerpoint' || fileType === 'application/vnd.openxmlformats-officedocument.presentationml.presentation') return 'bi-file-earmark-ppt';
        if (fileType === 'application/zip' || fileType === 'application/x-rar-compressed') return 'bi-file-earmark-zip';

        // Generic file icon as a fallback
        return 'bi-file-earmark';
    }

    function formatDateForDisplay(dateString: string | undefined): string {
        if (!dateString) return $t('common.notSet', { default: '-' });
        try {
            return new Date(dateString).toLocaleDateString();
        } catch (e) {
            return $t('common.invalidDate', { default: 'Invalid Date' });
        }
    }
</script>

<div
        class="modal-overlay"
        on:click={handleOverlayClick}
        on:keydown={handleOverlayKeyDown}
        role="dialog"
        aria-modal="true"
        aria-labelledby="attachments-dialog-title"
        tabindex="-1"
        bind:this={attachmentsModal}
>
    <div class="modal-content-container">
        <button type="button" class="close-dialog-button" on:click={closeDialog} aria-label={$t('common.close')}>×</button>
        <h3 id="attachments-dialog-title" class="dialog-title">{$t('attachmentsModal.title')}</h3>

        {#if isLoadingAttachments}
            <p class="status-message">{$t('common.loading')}...</p>
        {:else if loadingErrorMessage}
            <p class="status-message error-text">{loadingErrorMessage}</p>
        {:else if attachments.length === 0}
            <p class="status-message">{$t('attachmentsModal.noAttachmentsFound')}</p>
        {:else}
            <ul class="attachments-list">
                {#each attachments as attachment (attachment.id)}
                    <li class="attachment-item">
                        <div class="attachment-preview">
                            {#if getFilePreviewType(attachment.fileType) === 'image'}
                                <img
                                        src={getAttachmentViewUrl(attachment.id)}
                                        alt={$t('attachmentsModal.imagePreviewAlt', { values: { fileName: attachment.fileName } })}
                                        class="image-preview"
                                        loading="lazy"
                                />
                            {:else}
                                <i class="bi {getIconClassForFileType(attachment.fileType)} file-type-icon"></i>
                            {/if}
                        </div>
                        <div class="attachment-details">
                            <span class="attachment-filename" title={attachment.fileName}>{attachment.fileName}</span>
                            <span class="attachment-metadata">
                                {formatDisplayFileSize(attachment.fileSize)} | {$t('attachmentsModal.uploadedOn')}: {formatDateForDisplay(attachment.uploadedAt)}
                            </span>
                        </div>
                        <div class="attachment-actions">
                            {#if attachment.fileType && (attachment.fileType.startsWith('image/') || attachment.fileType === 'application/pdf' || attachment.fileType.startsWith('text/'))}
                                <button type="button" class="action-button view-button" on:click={() => handleViewClick(attachment)} title={$t('attachmentsModal.viewActionTitle', { values: {fileName: attachment.fileName}} )}>
                                    <i class="bi bi-eye"></i> {$t('attachmentsModal.view')}
                                </button>
                            {/if}
                            <button type="button" class="action-button download-button" on:click={() => handleDownloadClick(attachment)} title={$t('attachmentsModal.downloadActionTitle', { values: {fileName: attachment.fileName}} )}>
                                <i class="bi bi-download"></i> {$t('attachmentsModal.download')}
                            </button>
                        </div>
                    </li>
                {/each}
            </ul>
        {/if}
    </div>
</div>

<style>
    .modal-overlay {
        position: fixed;
        inset: 0;
        background-color: rgba(0, 0, 0, 0.65);
        display: flex;
        align-items: center;
        justify-content: center;
        z-index: 1050;
        padding: 1rem;
        outline: none;
    }

    .modal-content-container {
        background: white;
        padding: 1.5rem;
        border-radius: 8px;
        box-shadow: 0 5px 15px rgba(0, 0, 0, 0.25);
        width: 100%;
        max-width: 680px;
        max-height: 90vh;
        overflow-y: auto;
        position: relative;
        display: flex;
        flex-direction: column;
    }

    .close-dialog-button {
        position: absolute;
        top: 0.75rem;
        right: 0.75rem;
        background: transparent;
        border: none;
        font-size: 1.8rem;
        color: var(--gray-dark);
        cursor: pointer;
        line-height: 1;
        padding: 0.25rem;
    }
    .close-dialog-button:hover {
        color: var(--orange-dark);
    }

    .dialog-title {
        margin-top: 0;
        margin-bottom: 1.25rem;
        color: var(--orange-dark);
        text-align: center;
        font-size: 1.35rem;
        font-weight: 600;
    }

    .status-message {
        text-align: center;
        padding: 1.5rem 0;
        color: var(--gray-dark);
        font-style: italic;
    }
    .error-text {
        color: #c0392b;
        font-style: normal;
        font-weight: 500;
    }

    .attachments-list {
        list-style: none;
        padding: 0;
        margin: 0;
        flex-grow: 1;
    }

    .attachment-item {
        display: flex;
        align-items: center;
        padding: 0.8rem 0.25rem;
        border-bottom: 1px solid var(--gray-lightest, #f1f1f1);
        gap: 1rem;
    }
    .attachment-item:last-child {
        border-bottom: none;
    }
    .attachment-details {
        flex-grow: 1;
        min-width: 0;
    }
    .attachment-filename {
        display: block;
        font-weight: 500;
        color: var(--text);
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        margin-bottom: 0.2rem;
        font-size: 0.9rem;
    }
    .attachment-metadata {
        font-size: 0.75rem;
        color: var(--gray-dark);
    }

    .attachment-actions {
        display: flex;
        flex-direction: column;
        gap: 0.4rem;
        flex-shrink: 0;
    }
    .action-button {
        padding: 0.35rem 0.7rem;
        border: 1px solid var(--gray);
        border-radius: 4px;
        cursor: pointer;
        font-size: 0.8rem;
        font-weight: 500;
        background-color: white;
        color: var(--text);
        display: inline-flex;
        align-items: center;
        justify-content: center;
        gap: 0.3rem;
        transition: background-color 0.2s, color 0.2s, border-color 0.2s, box-shadow 0.2s;
        min-width: 90px;
    }
    .action-button i {
        font-size: 0.9em;
    }
    .action-button:hover {
        box-shadow: 0 1px 3px rgba(0,0,0,0.1);
    }
    .view-button {
        border-color: var(--orange);
        color: var(--orange-dark);
    }
    .view-button:hover {
        background-color: var(--orange-light);
    }
    .download-button {
        border-color: var(--gray-dark);
        color: var(--gray-dark);
    }
    .download-button:hover {
        background-color: var(--gray-lightest);
        color: var(--text);
        border-color: var(--gray);
    }
    .file-type-icon {
        font-size: 2.2rem;
        color: var(--orange);
    }

    .attachment-preview {
        flex-shrink: 0;
        width: 60px;
        height: 60px;
        display: flex;
        align-items: center;
        justify-content: center;
        background-color: var(--gray-lightest, #f7f7f7);
        border-radius: 4px;
        overflow: hidden;
    }
    .image-preview {
        max-width: 100%;
        max-height: 100%;
        object-fit: cover;
    }
</style>