<script lang="ts">
    import {t} from 'svelte-i18n';
    import type {Category, Language, Provider, Resource, ResourceFormData, Role} from '$lib/types/resource';
    import {showToast} from '$lib/stores/toast';
    import {uploadResource} from '$lib/web/resource-service';

    export let visible = false;
    export let onUploaded: (resource: Resource) => void;
    export let onClose: () => void;

    let title = '';
    let description = '';
    let category: Category | '' = '';
    let language: Language | '' = '';
    let provider: Provider | '' = '';
    let selectedRoles: Role[] = [];
    let alreadySelectedFiles: File[] = [];

    const availableRoles: { value: Role, labelKey: string }[] = [
        {value: 'MENTOR_COACH', labelKey: 'roles.mentor_coach'},
        {value: 'MENTEE_COACHEE', labelKey: 'roles.mentee_coachee'},
    ]

    let touched = {
        title: false,
        description: false,
        files: false,
        category: false,
        language: false,
        provider: false,
        roles: false
    };
    // Visual feedback for drag-and-drop
    let isDraggingOver = false;

    // This is bind to the form
    let formElement: HTMLFormElement;
    // $: means I'm using a reactive block and Svelte turns it into: "when any of these changes, re-run this block
    // after the DOM is updated. If i remove it, i get error on the form because that is rendered only after Svelte
    // has mounted the form.
    $: if (visible && formElement) {
        const firstInput = formElement.querySelector('input') as HTMLElement;
        firstInput?.focus();
    }

    // typeof means build a type from its structure so for example { title: boolean } instead of false.
    // keyof takes an object type and produces a union of its keys as string literal types: 'title' | 'description'..
    function markTouched(field: keyof typeof touched) {
        touched[field] = true;
    }

    function resetForm() {
        title = '';
        description = '';
        category = '';
        language = '';
        provider = '';
        selectedRoles = [];
        alreadySelectedFiles = [];
        isDraggingOver = false;

        Object.keys(touched).forEach(key => (touched[key as keyof typeof touched] = false));

        const fileInputEl = document.getElementById('form-input-file') as HTMLInputElement;
        if (fileInputEl) fileInputEl.value = '';
    }

    function formIsValid() {
        return (
            title.trim() &&
            description.trim() &&
            category &&
            language &&
            provider &&
            selectedRoles.length > 0 &&
            alreadySelectedFiles.length > 0
        );
    }

    async function onSubmit() {
        // This is to show the errors when clicking upload, otherwise validation errors wouldn't appear
        // When running formIsValid() i would get a false without UI errors
        Object.keys(touched).forEach(key => markTouched(key as keyof typeof touched));
        if (!formIsValid()) {
            showToast($t('upload.validationError'), 'error');
            return;
        }
        const dto: ResourceFormData = {
            title: title.trim(),
            description: description.trim(),
            category: category as Category,
            language: language as Language,
            provider: provider as Provider,
            roles: selectedRoles as Role[],
            files: alreadySelectedFiles
        };
        try {
            const created = await uploadResource(dto);
            showToast($t('upload.success'), 'success');
            onUploaded(created);
            resetForm();
            onClose();
        } catch (e) {
            showToast((e as Error).message || $t('upload.genericError'), 'error');
        }
    }

    function doClose() {
        resetForm();
        onClose();
    }

    function onOverlayKey(e: KeyboardEvent) {
        if (e.key === 'Escape') {
            doClose();
        }
    }

    function onOverlayClick(e: MouseEvent) {
        if ((e.target as HTMLElement).classList.contains('overlay')) {
            doClose();
        }
    }

    // Handles files from both <input type="file"> and drag-and-drop
    function processSelectedFiles(filesToProcess: FileList | null) {
        if (filesToProcess) {
            // Combine with already selected files and ensure uniqueness
            const combinedFiles = [...alreadySelectedFiles, ...filesToProcess];
            const uniqueFilesMap = new Map<string, File>();
            combinedFiles.forEach(file => {
                const key = `${file.name}-${file.size}-${file.lastModified}`;
                if (!uniqueFilesMap.has(key)) {
                    uniqueFilesMap.set(key, file);
                }
            });
            alreadySelectedFiles = Array.from(uniqueFilesMap.values());
        }
    }

    function handleFileInputChange(event: Event) {
        const input = event.target as HTMLInputElement;
        processSelectedFiles(input.files);
        markTouched('files');
    }

    function removeFile(fileToRemove: File) {
        alreadySelectedFiles = alreadySelectedFiles.filter(file => file !== fileToRemove);
        // Re-touch if all files removed to show error if needed
        markTouched('files');
    }

    function formatFileSize(bytes: number, decimals = 1): string {
        const units = ['Bytes', 'KB', 'MB', 'GB', 'TB'];

        if (bytes === 0) return '0 Bytes';
        const k = 1024;
        const dm = decimals < 0 ? 0 : decimals;
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return `${parseFloat((bytes / Math.pow(k, i)).toFixed(dm))} ${units[i]}`;
    }

    function handleDragEnter() {
        isDraggingOver = true;
    }

    function handleDragOver() {
        isDraggingOver = true;
    }

    function handleDragLeave(event: DragEvent) {
        const dropZone = event.currentTarget as HTMLElement;
        // relatedTarget is an element that the mouse is now moving into.
        // If both are true: It means the mouse has left the dropZone element itself but has moved onto one of its children.
        // This is not a "true" leave from the conceptual drop area. So, we return; and do not set isDraggingOver = false.
        if (event.relatedTarget && dropZone.contains(event.relatedTarget as Node)) {
            return;
        }
        isDraggingOver = false;
    }

    function handleDrop(event: DragEvent) {
        isDraggingOver = false;
        if (event.dataTransfer?.files && event.dataTransfer.files.length > 0) {
            processSelectedFiles(event.dataTransfer.files);
            markTouched('files');
        }
    }
</script>

{#if visible}
    <!--  role, aria.. for SR and accessibility -->
    <!--  role=dialog for accessibility so u say that a given part of the page is acting -->
    <!--  tabindex -1 because elements with role dialog must have a tabindex  -->
    <!--  aria-modal=true means that while the dialog is open the rest of the page is inert (not interactive)  -->
    <div
            class="overlay"
            role="dialog"
            aria-modal="true"
            aria-labelledby="upload-form-title"
            tabindex="-1"
            on:click={onOverlayClick}
            on:keydown={onOverlayKey}
    >
        <!-- Usually on forms u should add preventDefault to avoid unnecessary page reloads -->
        <!-- novalidate is to disable browser's build-in HTML5 validation UI -->
        <form class="form-card" on:submit|preventDefault={onSubmit} novalidate bind:this={formElement}>
            <button type="button" class="close-btn" aria-label={$t('common.close')} on:click={doClose}>×</button>
            <h3 id="upload-form-title">{$t('upload.formTitle')}</h3>

            <!-- Wrapped in labels for accessibility because clicking a label focusses the input -->
            <label>
                <input type="text" bind:value={title} placeholder={$t('upload.placeholders.title')}
                       on:blur={() => markTouched('title')} class:input-error={touched.title && !title.trim()}
                       required/>
                {#if touched.title && !title.trim()}
                    <small class="error-message-inline">{$t('validation.required', {values: {field: $t('upload.labels.title')}})}</small>
                {/if}
            </label>
            <label>
                <textarea bind:value={description} placeholder={$t('upload.placeholders.description')}
                          on:blur={() => markTouched('description')}
                          class:input-error={touched.description && !description.trim()} required></textarea>
                {#if touched.description && !description.trim()}
                    <small class="error-message-inline">{$t('validation.required', {values: {field: $t('upload.labels.description')}})}</small>
                {/if}
            </label>
            <label>
                <select bind:value={category} on:blur={() => markTouched('category')} required
                        class:input-error={touched.category && !category}>
                    <option disabled value="">{$t('upload.placeholders.selectCategory')}</option>
                    <option value="LEADERSHIP">{$t('category.leadership')}</option>
                    <option value="MANAGING_COMPLEXITY">{$t('category.managing_complexity')}</option>
                </select>
                {#if touched.category && !category}
                    <small class="error-message-inline">{$t('validation.required', {values: {field: $t('upload.labels.category')}})}</small>
                {/if}
            </label>
            <label>
                <select bind:value={language} on:blur={() => markTouched('language')} required
                        class:input-error={touched.language && !language}>
                    <option disabled value="">{$t('upload.placeholders.selectLanguage')}</option>
                    <option value="EN">{$t('language.en')}</option>
                    <option value="IT">{$t('language.it')}</option>
                    <option value="ES">{$t('language.es')}</option>
                </select>
                {#if touched.language && !language}
                    <small class="error-message-inline">{$t('validation.required', {values: {field: $t('upload.labels.language')}})}</small>
                {/if}
            </label>
            <label>
                <select bind:value={provider} on:blur={() => markTouched('provider')} required
                        class:input-error={touched.provider && !provider}>
                    <option disabled value="">{$t('upload.placeholders.selectProvider')}</option>
                    <option value="SKILLA">{$t('provider.skilla')}</option>
                    <option value="LINKEDIN">{$t('provider.linkedin')}</option>
                    <option value="PACK">{$t('provider.pack')}</option>
                    <option value="MENTOR">{$t('provider.mentor')}</option>
                </select>
                {#if touched.provider && !provider}
                    <small class="error-message-inline">{$t('validation.required', {values: {field: $t('upload.labels.provider')}})}</small>
                {/if}
            </label>
            <label>

                <fieldset>
                    <legend> {$t('upload.labels.roles')}*</legend>
                    {#each availableRoles as roleOption (roleOption.value)}
                        <div class="input-options-container">
                            <!-- bind:group is Svelte magic, when a checkbox is checked it's added to selectedRoles array -->
                            <input class="checkbox"
                                   type="checkbox"
                                   value={roleOption.value}
                                   bind:group={selectedRoles}
                                   on:blur={() => markTouched('roles')}
                            />
                            <span>{$t(roleOption.labelKey, {default: roleOption.value.replace('_', '/')})}</span>
                        </div>
                    {/each}
                </fieldset>

                {#if touched.roles && !selectedRoles}
                    <small class="error-message-inline roles-error">{$t('validation.minSelection', {
                        values: {
                            field: $t('upload.labels.roles'),
                            count: 1
                        }
                    })}</small>
                {/if}
            </label>

            <!-- You also need to call event.preventDefault() in the drop event handler.
            The browser's default action for a drop (especially of files) might be to try and open the file or navigate to its local path.
            preventDefault() stops this. -->
            <div
                    class="file-input-section"
                    class:dragging-over={isDraggingOver}
                    on:dragenter|preventDefault|stopPropagation={handleDragEnter}
                    on:dragover|preventDefault|stopPropagation={handleDragOver}
                    on:dragleave|preventDefault|stopPropagation={handleDragLeave}
                    on:drop|preventDefault|stopPropagation={handleDrop}
                    role="group"
                    aria-labelledby="file-drop-zone-label"
                    tabindex="-1"
            >
                <input
                        id="form-input-file"
                        type="file"
                        multiple
                        hidden
                        on:change={handleFileInputChange}
                        on:blur={() => markTouched('files')}
                        aria-hidden="true"
                        accept=".pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.txt,.png,.jpg,.jpeg,.gif,video/*,audio/*"
                />
                <div id="file-drop-zone-label" class="drag-drop-cue">
                    <!-- When i drag over -->
                    {#if isDraggingOver}
                        <span class="bold-orange-text">{$t('upload.dropFilesHere')}</span>
                        <!-- When i just open the form -->
                    {:else if alreadySelectedFiles.length === 0}
                        <i class="bi bi-cloud-arrow-up-fill drag-icon"></i>
                        {$t('upload.dragDropOr')}
                        <span class="visually-hidden">{$t('upload.selectFiles')}</span>
                        <!-- For screen readers, button below is primary -->
                    {:else}
                        <i class="bi bi-cloud-arrow-up-fill drag-icon"></i>
                        {$t('upload.dragDropMoreOr')}
                        <span class="visually-hidden">{$t('upload.selectMoreFiles')}</span>
                    {/if}
                </div>

                {#if alreadySelectedFiles.length === 0 && !isDraggingOver && touched.files}
                    <span class="file-error-text">
                        {$t('upload.noFilesSelected')}*
                    </span>
                    <small class="file-error-text">
                        {$t('validation.minFiles', {values: {count: 1}})}
                    </small>
                {/if}

                {#if alreadySelectedFiles.length > 0}
                    <ul class="selected-files-list">
                        {#each alreadySelectedFiles as f (f.name + f.lastModified + f.size)}
                            <li class="selected-file-item">
                                <span class="file-item-name" title={f.name}>{f.name}</span>
                                <span class="file-item-size">({formatFileSize(f.size)})</span>
                                <button type="button" class="remove-file-btn" on:click={() => removeFile(f)}
                                        aria-label={$t('upload.removeFile', { values: {file: f.name}})}>×
                                </button>
                            </li>
                        {/each}
                    </ul>
                {/if}

                <button
                        type="button"
                        class="select-file-btn"
                        on:click={() => document.getElementById('form-input-file')?.click()}
                        aria-describedby={alreadySelectedFiles.length === 0 ? "file-drop-zone-label" : undefined}
                >
                    {alreadySelectedFiles.length > 0 ? $t('upload.selectMoreFiles') : $t('upload.selectFiles')}
                </button>
            </div>

            <button type="submit" class="upload-btn form-submit-btn" disabled={!formIsValid()}>
                {$t('upload.submit')}
            </button>
        </form>
    </div>
{/if}

<style>
    :root {
        --color-error: #e53935;
    }

    /* --- Modal Structure --- */
    .overlay {
        position: fixed;
        inset: 0;
        z-index: 1000;
        display: flex;
        align-items: flex-start;
        justify-content: center;
        padding: 1.5rem 1rem;
        background: rgba(0, 0, 0, 0.6);
    }

    .form-card {
        position: relative;
        display: flex;
        flex-direction: column;
        gap: 0.5rem;
        width: 100%;
        max-width: 430px;
        max-height: 80vh;
        padding: 1rem 1.5rem;
        background: white;
        border-radius: 8px;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.25);
        overflow-y: auto;
        margin-top: 1.5rem;
        margin-bottom: 1.5rem;
    }

    /* --- Form Header & Close Button --- */
    .form-card h3 {
        margin: 0 0 0.6rem;
        color: var(--orange-dark);
        font-size: 1.25rem;
        text-align: center;
    }

    .close-btn {
        position: absolute;
        top: 0.4rem;
        right: 0.6rem;
        padding: 0.1rem;
        background: transparent;
        border: none;
        color: var(--gray-dark);
        font-size: 1.7rem;
        line-height: 1;
        cursor: pointer;
    }

    .close-btn:hover {
        color: var(--orange-dark);
    }

    /* --- Form Fields --- */
    .form-card label {
        display: flex;
        flex-direction: column;
        gap: 0.15rem;
        color: var(--text);
        font-size: 0.85rem;
    }

    .form-card input,
    .form-card textarea,
    .form-card select {
        width: 100%;
        padding: 0.5rem 0.6rem;
        font: inherit;
        font-size: 0.9rem;
        background-color: #fff;
        border: 1px solid var(--gray);
        border-radius: 3px;
        box-sizing: border-box;
        transition: border-color 0.2s, box-shadow 0.2s;
    }

    .form-card textarea {
        resize: vertical;
        min-height: 50px;
        height: 60px;
    }

    .form-card input::placeholder, .form-card textarea::placeholder {
        color: var(--gray-dark);
        font-size: 0.85rem;
    }

    .form-card select option[disabled] {
        color: var(--gray-dark);
    }

    .form-card input:focus, .form-card textarea:focus, .form-card select:focus {
        border-color: var(--orange);
        box-shadow: 0 0 0 2px var(--orange-light);
        outline: none;
    }

    /* --- File Input & Drag-Drop Section --- */
    .file-input-section {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 0.5rem;
        margin-top: 0.3rem;
        margin-bottom: 0.3rem;
        padding: 0.6rem;
        background-color: var(--gray-lightest, #f8f9fa);
        border: 2px dashed var(--gray-light);
        border-radius: 4px;
        transition: border-color 0.2s, background-color 0.2s;
        text-align: center;
        cursor: default;
    }

    .file-input-section:focus-within,
    .file-input-section.dragging-over {
        border-color: var(--orange);
        background-color: var(--orange-lightest, #fff3e0);
        box-shadow: 0 0 0 1px var(--orange-light);
        outline: none;
    }

    .drag-drop-cue {
        padding: 0.1rem 0;
        color: var(--gray-dark);
        font-size: 0.8rem;
        pointer-events: none;
    }

    .drag-drop-cue .drag-icon {
        font-size: 1.3rem;
        color: var(--orange-dark);
        display: block;
        margin-bottom: 0.1rem;
    }

    .bold-orange-text {
        font-weight: bold;
        color: var(--orange-dark);
        font-size: 0.9rem;
    }

    .input-options-container {
        display: flex;
    }

    .checkbox {
        width: 20px !important;
    }

    .selected-files-list {
        width: 100%;
        max-height: 60px;
        margin: 0.5rem 0 0;
        padding: 0;
        font-size: 0.8rem;
        list-style: none;
        background-color: #fff;
        border: 1px solid var(--gray-light, #e0e0e0);
        border-radius: 4px;
        overflow-y: auto;
        box-shadow: inset 0 1px 2px rgba(0, 0, 0, 0.05);
    }

    .selected-file-item {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 0.5rem 0.6rem;
        border-bottom: 1px solid var(--gray-lightest, #f1f1f1);
        transition: background-color 0.2s;
    }

    .selected-file-item:last-child {
        border-bottom: none;
    }

    .file-item-name {
        flex-grow: 1;
        margin-right: 0.5rem;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        color: var(--text, #333);
        font-weight: 500;
    }

    .file-item-size {
        flex-shrink: 0;
        margin-right: 0.6rem;
        color: var(--gray-dark, #757575);
        font-size: 0.9em;
    }

    .remove-file-btn {
        flex-shrink: 0;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        width: 22px;
        height: 22px;
        padding: 0;
        background-color: transparent;
        border: 1px solid transparent;
        color: var(--gray-dark, #757575);
        font-size: 1.2rem;
        line-height: 1;
        cursor: pointer;
        border-radius: 50%;
        transition: background-color 0.2s, color 0.2s, border-color 0.2s;
    }

    .remove-file-btn:hover {
        color: var(--color-error, #e53935);
    }


    /* --- Action Buttons --- */
    .select-file-btn, .form-submit-btn {
        padding: 0.4rem 0.8rem;
        border-radius: 3px;
        font-weight: bold;
        cursor: pointer;
        transition: background-color 0.2s, color 0.2s;
        line-height: normal;
        vertical-align: middle;
        font-size: 0.85rem;
    }

    .select-file-btn {
        background: var(--orange-light);
        border: 1px solid var(--orange);
        color: var(--orange-dark);
    }

    .select-file-btn:hover {
        background: var(--orange);
        color: white;
    }

    .form-submit-btn {
        width: 100%;
        padding: 0.6rem;
        background: var(--orange);
        border: none;
        color: white;
        font-size: 0.9rem;
        margin-top: 0.4rem;
    }

    .form-submit-btn:hover:not(:disabled) {
        background: var(--orange-dark);
    }

    .form-submit-btn:disabled {
        background: var(--gray);
        cursor: not-allowed;
        opacity: 0.7;
    }

    /* --- Validation & Error States --- */
    .input-error {
        border-color: var(--color-error, #e53935) !important;
        box-shadow: 0 0 0 1px var(--color-error, #e53935) !important;
    }

    .error-message-inline, .file-error-text {
        color: var(--color-error, #e53935);
    }

    .error-message-inline {
        margin-top: 0.1rem;
        font-size: 0.7rem
    }

    .file-error-text {
        font-size: 0.75rem;
        font-weight: 500;
    }

    /* --- Utility for Screen Readers --- */
    .visually-hidden {
        border: 0;
        clip: rect(0 0 0 0);
        height: 1px;
        margin: -1px;
        overflow: hidden;
        padding: 0;
        position: absolute;
        width: 1px;
        white-space: nowrap;
    }
</style>