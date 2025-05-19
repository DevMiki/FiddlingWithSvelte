<script lang="ts">
    import {t, locale as i18nLocaleStore} from 'svelte-i18n';
    import type {Resource} from '$lib/types/resource';
    import AttachmentsModal from './AttachmentsModal.svelte';

    // t and locale are writable svelte store
    // Variables with export are properties
    export let onOpenForm: () => void = () => {};
    export let resources: Resource[] = [];

    let showAttachmentsModal = false;
    let selectedResourceIdForModal: number | null = null;

    type SortableKey = keyof Pick<Resource, 'title' | 'category' | 'language' | 'provider' | 'roles' | 'attachmentCount'>;
    type SortDirection = 'asc' | 'desc';

    let sortKey: SortableKey | null = null;
    let sortDirection: SortDirection = 'asc';

    function handleCloseAttachmentsModal() {
        showAttachmentsModal = false;
        selectedResourceIdForModal = null;
    }

    // Helper function to get the translated enum value because sorting the resource directly like
    // resource.category would result in a mismatch since you'd be sorting by KEY and not VALUE.
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

    function getTranslatedEnum(prefix: string, value: string | null | undefined, fallback = '-') {
        if (value) {
            const key = value.toLowerCase();
            return $t(`${prefix}.${key}`, {default: fallback});
        }
        return $t('common.notSet', {default: fallback});
    }

    // Called when a table header is clicked
    function handleSort(key: SortableKey) {
        if (sortKey === key) {
            sortDirection = sortDirection === 'asc' ? 'desc' : 'asc';
        } else {
            sortKey = key;
            sortDirection = 'desc';
        }
    }

    function getSortIndicator(key: SortableKey): string {
        if (sortKey !== key) return '↕'; // Unsorted
        return sortDirection === 'asc' ? '▲' : '▼';
    }


    // This is Svelte syntax for reactive declarations. sortedResources is a derived variable, its value depends on resources,
    // sortKey and sortDirection. Whenever any of these dependencies change, Svelte will automatically re-run the code to the right of the '='
    // and update sortedResources.
    // The big benefit of this $: reactive declaration is that i don't have to invoke a function every time the user clicks on a column header
    // or when the data change. This is synced automatically.
    // sortKey and sortDirection are changed when a table header is clicked
    // resources when you load or upload data
    // it means: 'Svelte', please watch these variables, and if they change, redo this calculation and update sortedResources
    // remember that the function the sort takes, is just the logic that the internal algorithm will use.
    $: sortedResources = [...resources].sort((firstResource, secondResource) => {
        if (!sortKey) return 0; // If you don't click, then don't sort

        const firstCompareValue = getSortableValue(firstResource, sortKey);
        const secondCompareValue = getSortableValue(secondResource, sortKey);

        // This is useful to flip the result of a comparison if a descending order is needed
        const sortOrderFactor = sortDirection === 'asc' ? 1 : -1;

        // Js compares these lexicographically like a dictionary so:
        // cat < category, 10 < 2, a < b...
        // If we want to see apple to come first of banana, then we want to return -1 and since the order is ascending
        // we want to keep it like this, so * sortDirection where sortDirection === 'asc' ? 1
        if (firstCompareValue < secondCompareValue) {
            return -1 * sortOrderFactor;
        }
        if (firstCompareValue > secondCompareValue) {
            return 1 * sortOrderFactor;
        }
        return 0;
    });

    function viewAttachments(resourceId: number) {
        selectedResourceIdForModal = resourceId;
        showAttachmentsModal = true;
    }

    function closeAttachmentsModal() {
        showAttachmentsModal = false;
        selectedResourceIdForModal = null;
    }

    const supportedLocales = [
        {code: 'en', nameKey: 'languageSwitcher.english'},
        {code: 'it', nameKey: 'languageSwitcher.italian'},
        {code: 'es', nameKey: 'languageSwitcher.spanish'},
    ];
</script>

<div class="table-container">
    <div class="table-header">
        <h2>{$t('table.title')}</h2>
        <div class="header-actions">
            <div>
                <!-- The value of the 'for' should be the same of the 'id' associated to.
                with this trick, you can add accessibility for screen readers so if someone uses it he doesn't hear
                just "combo-box" but something more valuable like what this is about -->
                <label for="language-select" class="sr-only">{$t('languageSwitcher.selectLabel')}</label>
                <!-- i18nLocaleStore is a Svelte writable store: it holds a value and allows reactive subscriptions to that value.
                 When you prefix a name with $, then Svelte automatically handles subscribing and unsubscribing to that store.
                 this $i18nLocaleStore gives me the current value and bind:value={$i18nLocaleStore} it's double binding.
                 When the app starts, the value of the store is set to the select. When the user clicks on the select instead,
                 the value of the option is set to the store, this triggers the svelte-i18n and the text is re-rendered. -->
                <select id="language-select"
                        bind:value={$i18nLocaleStore}
                        class="language-select">
                    {#each supportedLocales as loc (loc.code)}
                        <option value={loc.code}>{$t(loc.nameKey)}</option>
                    {/each}
                </select>
            </div>
            <!-- on: is the Svelte directive that tells Svelte you want to listen for a DOM event.
             click is the name of the DOM event you want to listen to!
             {onOpenForm} is the handler function you want to execute when the event occurs. It can be an inline function too -->
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
                <!-- Questi title sono ciò che vedi quando metti il mouse sopra ogni header, tipo tooltip
                il metodo getSortIndicator quindi mi fa solo visualizzare il corretto simbolo di ordinamento on hover -->
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
                    {$t('table.cols.attachments')} <span class="sort-indicator">{getSortIndicator('attachmentCount')}</span>
                </th>
            </tr>
            </thead>

            <tbody>

            {#each sortedResources as sortedResource (sortedResource.id)}
            <!-- By default, HTML table rows (<tr>) are not focusable elements.
            You can't typically "Tab" to a table row and have it highlight or be interactive via the keyboard.-->
            <!-- Adding tabindex="0" to the <tr> makes each row in your table focusable.
            This is often done to improve keyboard accessibility for interactive tables.
            Useful for SR too and custom keyboard interactions -->
                <tr tabindex="0">
                    <td>{sortedResource.title}</td>
                    <td>{getTranslatedEnum('category', sortedResource.category)}</td>
                    <td>{getTranslatedEnum('language', sortedResource.language)}</td>
                    <td>{getTranslatedEnum('provider', sortedResource.provider)}</td>
                    <td>
                        {sortedResource.roles && sortedResource.roles.length > 0
                            ? sortedResource.roles.map(role => getTranslatedEnum('roles', role)).join(', ')
                            : $t('common.notSet', {default: '-'})}
                    </td>
                    <td class="attachments-cell">
                        {#if sortedResource.attachmentCount > 0}
                            <!--  stopPropagation  is like putting a lid on the event so it doesn't spill over to the elements outside the button.
                            It stops the event right at the button and prevents parent elements from reacting to that specific click,
                             which helps avoid unintended side effects if those parent elements also have click handlers -->
                            <button
                                    type="button"
                                    class="view-attachments-btn"
                                    title={$t('table.viewAttachments')}
                                    on:click|stopPropagation={() => viewAttachments(sortedResource.id)}
                            >
                                <!-- Using an embedded SVG is good for performance since the browser shouldn't do more requests
                                 moreover you can style it inline
                                 xmlns: namespace declaration for browsers to interpret the markup as SVG
                                 The d attribute (which stands for "data") contains a series of commands and coordinates that define the shape of the path
                                 Using a .png would work but not scalable and no cool props like fill -->
                                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 16 16"
                                      class="margin-right">
                                    <path d="M4.5 3a2.5 2.5 0 0 1 5 0v9a1.5 1.5 0 0 1-3 0V5a.5.5 0 0 1 1 0v7a.5.5 0 0 0 1 0V3a1.5 1.5 0 1 0-3 0v9a2.5 2.5 0 0 0 5 0V5a.5.5 0 0 1 1 0v7a3.5 3.5 0 1 1-7 0V3z"/>
                                </svg>
                                <span class="count">({sortedResource.attachmentCount})</span>
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
        /* Without this the header would overflow the container */
        overflow-x: auto;
    }

    /* --- Table Header --- */
    /* The style of the part above the table header, like the buttons and the title */
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

    .view-attachments-btn .margin-right {
        width: 1em;
        height: 1em;
        fill: currentColor;
    }

    .view-attachments-btn .count {
        font-size: 0.85em;
        font-weight: normal;
    }

    /* The chevron down icon */
    .language-select {
        padding: 0.4rem 2rem 0.4rem 0.6rem;
        border: 1px solid var(--orange-dark);
        border-radius: 4px;
        background-color: white;
        color: var(--orange-dark);
        font-weight: 500;
        font-size: inherit;
        cursor: pointer;
        appearance: none;
        min-width: 130px;
        background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='16' height='16' fill='%23e65100' viewBox='0 0 16 16'%3E%3Cpath fill-rule='evenodd' d='M1.646 4.646a.5.5 0 0 1 .708 0L8 10.293l5.646-5.647a.5.5 0 0 1 .708.708l-6 6a.5.5 0 0 1-.708 0l-6-6a.5.5 0 0 1 0-.708z'/%3E%3C/svg%3E");
        background-repeat: no-repeat;
        background-position: right 0.5rem center;
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
        clip: rect(0, 0, 0, 0);
        white-space: nowrap;
    }
</style>