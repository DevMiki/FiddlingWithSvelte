<script lang="ts">
    import Toast from '$lib/components/Toast.svelte';
    import { onMount } from 'svelte';
    import ResourceTable from '$lib/components/ResourceTable.svelte';
    import ResourceForm from '$lib/components/ResourceForm.svelte';
    import type {Resource} from "$lib/types/resource";
    import {listResources} from "$lib/web/resource-service";

    let resources: Resource[] = [];
    let showForm = false;

    async function loadData() {
        try {
            resources = await listResources();
        } catch (e) {
            console.error(e);
        }
    }

    onMount(loadData);

    function handleUploaded(r: Resource) {
        resources = [r, ...resources];
    }

    function openUploadForm() {
        showForm = true;
    }
</script>

<Toast />


<ResourceForm visible={showForm}
              onUploaded={handleUploaded}
              onClose={() => showForm = false} />

<div class="page-content-wrapper">
    <ResourceTable {resources} onOpenForm={openUploadForm} />
</div>

<style>
    .page-content-wrapper {
        max-width: 80%;
        margin-left: auto;
        margin-right: auto;
        padding: 2rem;
    }
</style>
