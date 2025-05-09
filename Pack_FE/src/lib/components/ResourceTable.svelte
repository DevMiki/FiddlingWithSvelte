<script lang="ts">
    import {t, locale} from 'svelte-i18n';
    import type {Resource} from '$lib/types/resource';
    import AttachmentsModal from './AttachmentsModal.svelte';

    export let onOpenForm: () => void = () => {
    };
    export let resources: Resource[] = [];

    let showAttachmentsModal = false;
    let selectedResourceIdForModal: number | null = null;

    type SortableKey =
        keyof Pick<Resource, 'title' | 'category' | 'language' | 'provider'>
        | 'roles'
        | 'attachmentCount';
    type SortDirection = 'asc' | 'desc';

    let sortKey: SortableKey | null = null;
    let sortDirection: SortDirection = 'asc';

    function handleCloseAttachmentsModal() {
        showAttachmentsModal = false;
        selectedResourceIdForModal = null;
    }

    function getSortableValue(resource: Resource, key: SortableKey): string | number {
        switch (key) {
            case 'category':
            case 'language':
            case 'provider':
                return getTranslatedEnum(key, resource[key], '').toLowerCase();
            case 'roles':
                return resource.roles && resource.roles.length > 0
                    ? resource.roles.map(code => getTranslatedEnum('roles', code)).join(', ').toLowerCase()
                    : '';
            case 'attachmentCount':
                return resource.attachmentCount;
            case 'title':
            {
                const titleValue = resource[key];
                return titleValue.toLowerCase();
            }
            default:
                // If there was something not handled attempt direct access and lowercase if string
            {
                const value = resource[key as keyof Resource];
                if (typeof value === 'string') return value.toLowerCase();
                if (typeof value === 'number') return value;
                return '';
            }
        }
    }

    $: sortedResources = [...resources].sort((firstResource, secondResource) => {
        if (!sortKey) return 0; // If you don't click, then don't sort

        const firstCompareValue = getSortableValue(firstResource, sortKey);
        const secondCompareValue = getSortableValue(secondResource, sortKey);

        const sortOrderFactor = sortDirection === 'asc' ? 1 : -1;

        if (firstCompareValue < secondCompareValue) {
            return -1 * sortOrderFactor;
        }
        if (firstCompareValue > secondCompareValue) {
            return 1 * sortOrderFactor;
        }
        return 0;
    });

    function handleSort(key: SortableKey) {
        if (sortKey === key) {
            sortDirection = sortDirection === 'asc' ? 'desc' : 'asc';
        } else {
            sortKey = key;
            sortDirection = 'asc';
        }
    }

    function getSortIndicator(key: SortableKey): string {
        if (sortKey !== key) return '↕'; // Unsorted
        return sortDirection === 'asc' ? '▲' : '▼';
    }


    function viewAttachments(resourceId: number) {
        selectedResourceIdForModal = resourceId;
        showAttachmentsModal = true;
    }

    function closeAttachmentsModal() {
        showAttachmentsModal = false;
        selectedResourceIdForModal = null;
    }

    function getTranslatedEnum(prefix: string, value: string | null | undefined, fallback = '-') {
        if (value && value.trim() && value.toLowerCase() !== 'null') {
            const key = value.toLowerCase();
            return $t(`${prefix}.${key}`, {default: fallback});
        }
        return $t('common.notSet', {default: fallback});
    }

    const supportedLocales = [
        {code: 'en', nameKey: 'languageSwitcher.english'},
        {code: 'it', nameKey: 'languageSwitcher.italian'},
        {code: 'es', nameKey: 'languageSwitcher.spanish'},
    ];

    function handleLocaleChange(event: Event) {
        const newLocale = (event.target as HTMLSelectElement).value;
        if (newLocale) {
            locale.set(newLocale); // Update the svelte-i18n locale store
        }
    }
</script>

<div class="table-container">
    <div class="table-header">
        <h2>{$t('table.title')}</h2>
        <div class="header-actions">
            <div class="language-switcher-container">
                <label for="language-select" class="sr-only">{$t('languageSwitcher.selectLabel')}</label>
                <select id="language-select" bind:value={$locale} on:change={handleLocaleChange}
                        class="language-select">
                    {#each supportedLocales as loc (loc.code)}
                        <option value={loc.code}>{$t(loc.nameKey)}</option>
                    {/each}
                </select>
            </div>
            <button type="button" class="upload-btn" on:click={onOpenForm}>
                {$t('table.upload')}
            </button>
        </div>
    </div>

    {#if resources.length === 0}
        <p class="no-resources-message">{$t('table.noResources')}</p>
    {:else}
        <table>
            <thead>
            <tr>
                <th on:click={() => handleSort('title')} class:sortable={true}
                    title={$t('table.sort.title', {values: { order: getSortIndicator('title')}})}>
                    {$t('table.cols.title')} <span class="sort-indicator">{getSortIndicator('title')}</span>
                </th>
                <th on:click={() => handleSort('category')} class:sortable={true}
                    title={$t('table.sort.category', { values: { order: getSortIndicator('category')}})}>
                    {$t('table.cols.category')} <span class="sort-indicator">{getSortIndicator('category')}</span>
                </th>
                <th on:click={() => handleSort('language')} class:sortable={true}
                    title={$t('table.sort.language', { values: { order: getSortIndicator('language')}})}>
                    {$t('table.cols.language')} <span class="sort-indicator">{getSortIndicator('language')}</span>
                </th>
                <th on:click={() => handleSort('provider')} class:sortable={true}
                    title={$t('table.sort.provider', { values: { order: getSortIndicator('provider')}})}>
                    {$t('table.cols.provider')} <span class="sort-indicator">{getSortIndicator('provider')}</span>
                </th>
                <th on:click={() => handleSort('roles')} class:sortable={true}
                    title={$t('table.sort.roles', { values: { order: getSortIndicator('roles')}})}>
                    {$t('table.cols.roles')} <span class="sort-indicator">{getSortIndicator('roles')}</span>
                </th>
                <th class="attachments-col-header" on:click={() => handleSort('attachmentCount')} class:sortable={true}
                    title={$t('table.sort.attachments', { values: { order: getSortIndicator('attachmentCount')}})}>
                    {$t('table.cols.attachments')} <span
                        class="sort-indicator">{getSortIndicator('attachmentCount')}</span>
                </th>
            </tr>
            </thead>
            <tbody>

            {#each sortedResources as r (r.id)}
                <tr tabindex="0">
                    <td>{r.title}</td>
                    <td>{getTranslatedEnum('category', r.category)}</td>
                    <td>{getTranslatedEnum('language', r.language)}</td>
                    <td>{getTranslatedEnum('provider', r.provider)}</td>
                    <td>
                        {r.roles && r.roles.length > 0
                            ? r.roles.map(code => getTranslatedEnum('roles', code)).join(', ')
                            : $t('common.notSet', {default: '-'})}
                    </td>
                    <td class="attachments-cell">
                        {#if r.attachmentCount > 0}
                            <button
                                    type="button"
                                    class="view-attachments-btn"
                                    title={$t('table.viewAttachments')}
                                    on:click|stopPropagation={() => viewAttachments(r.id)}
                            >
                                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 16 16"
                                     fill="currentColor" class="icon">
                                    <path d="M4.5 3a2.5 2.5 0 0 1 5 0v9a1.5 1.5 0 0 1-3 0V5a.5.5 0 0 1 1 0v7a.5.5 0 0 0 1 0V3a1.5 1.5 0 1 0-3 0v9a2.5 2.5 0 0 0 5 0V5a.5.5 0 0 1 1 0v7a3.5 3.5 0 1 1-7 0V3z"/>
                                </svg>
                                <span class="count">({r.attachmentCount})</span>
                            </button>
                        {:else}
                            <span class="no-attachments">-</span>
                        {/if}
                    </td>
                </tr>
            {/each}
            </tbody>
        </table>
    {/if}
</div>

{#if showAttachmentsModal && selectedResourceIdForModal}
    <AttachmentsModal
            resourceId={selectedResourceIdForModal}
            on:close={closeAttachmentsModal}
            onCloseDialog={handleCloseAttachmentsModal}
    />
{/if}

<style>
    .table-container {
        background: white;
        border-radius: 8px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
        overflow-x: auto;
    }

    /* --- Table Header --- */
    .table-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        background: var(--orange-light);
        padding: 1rem;
        border-top-left-radius: 8px;
        border-top-right-radius: 8px;
    }

    .table-header h2 {
        margin: 0;
        color: var(--orange-dark);
        font-size: 1.5rem;
    }

    .header-actions {
        display: flex;
        align-items: center;
        gap: 0.75rem;
    }

    /* --- General Table Styling --- */
    table {
        width: 100%;
        border-spacing: 0;
        border-collapse: collapse;
    }

    tr {
        font-size: 0.8rem;
        font-weight: 400;
    }

    /* --- Table Head (thead) --- */
    thead {
        background: var(--orange);
    }

    thead th {
        color: white;
        text-align: left;
        padding: 0.75rem 1rem;
        font-weight: 500;
        font-size: 0.95rem;
        border-bottom: 2px solid var(--orange-dark);
        white-space: nowrap;
    }

    /* --- Table Body --- */
    tbody tr {
        transition: background 0.2s;
    }

    tbody tr:hover {
        background: var(--orange-light);
    }

    tbody td {
        padding: 0.75rem 1rem;
        text-align: left;
        color: var(--text);
        border-bottom: 1px solid var(--gray);
        vertical-align: middle;
    }


    tbody td:last-child {
        border-right: none;
    }

    /* --- Specific Column Styling --- */
    .attachments-col-header,
    .attachments-cell {
        text-align: center;
    }

    /* --- Buttons & Interactive Elements --- */
    .upload-btn {
        background: var(--orange);
        color: white;
        border: none;
        padding: 0.5rem 1rem;
        border-radius: 4px;
        cursor: pointer;
        font-weight: bold;
        white-space: nowrap;
        line-height: normal;
        vertical-align: middle;
    }

    .upload-btn:hover {
        background: var(--orange-dark);
    }

    .view-attachments-btn {
        background: transparent;
        border: 1px solid transparent;
        color: var(--orange-dark);
        cursor: pointer;
        padding: 0.3rem 0.5rem;
        border-radius: 4px;
        display: inline-flex;
        align-items: center;
        gap: 0.3rem;
        transition: background-color 0.2s, border-color 0.2s;
    }

    .view-attachments-btn:hover,
    .view-attachments-btn:focus {
        background-color: var(--orange-light);
        border-color: var(--orange);
    }

    .view-attachments-btn .icon {
        width: 1em;
        height: 1em;
        fill: currentColor;
    }

    .view-attachments-btn .count {
        font-size: 0.85em;
        font-weight: normal;
    }

    .language-switcher-container {
        position: relative;
    }

    .language-select {
        padding: 0.4rem 0.6rem;
        padding-right: 2rem;
        border: 1px solid var(--orange-dark);
        border-radius: 4px;
        background-color: white;
        color: var(--orange-dark);
        font-weight: 500;
        cursor: pointer;
        appearance: none;
        min-width: 130px;
        background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='16' height='16' fill='%23e65100' viewBox='0 0 16 16'%3E%3Cpath fill-rule='evenodd' d='M1.646 4.646a.5.5 0 0 1 .708 0L8 10.293l5.646-5.647a.5.5 0 0 1 .708.708l-6 6a.5.5 0 0 1-.708 0l-6-6a.5.5 0 0 1 0-.708z'/%3E%3C/svg%3E");
        background-repeat: no-repeat;
        background-position: right 0.5rem center;
        vertical-align: middle;
        line-height: normal;
        font-size: inherit;
    }

    .language-select:hover {
        border-color: var(--orange);
    }

    .language-select:focus {
        outline: 2px solid var(--orange-light);
        border-color: var(--orange);
    }

    /* --- Sortable Headers --- */
    .sortable {
        cursor: pointer;
        user-select: none;
    }

    .sortable:hover {
        background-color: var(--orange-dark);
    }

    .sort-indicator {
        margin-left: 0.4em;
        color: #af2a00;
        display: inline-block;
        width: 1em; /* Reserve space for the indicator */
        text-align: center;
        font-size: 0.9em; /* Relative to th font-size */
        line-height: 1; /* To prevent affecting th line height */
        vertical-align: middle;
        font-weight: bold; /* Added based on "boldish arrows" request */
    }

    .no-attachments {
        color: var(--gray-dark);
        font-style: italic;
    }

    .no-resources-message {
        padding: 2rem;
        text-align: center;
        color: var(--gray-dark);
    }

    /* --- Utilities --- */
    .sr-only {
        position: absolute;
        width: 1px;
        height: 1px;
        padding: 0;
        margin: -1px;
        overflow: hidden;
        clip: rect(0, 0, 0, 0);
        white-space: nowrap;
        border-width: 0;
    }
</style>