<script lang="ts">
    // $lib path is SvelteKit convention for easily accessing src/lib directory
    import Toast from '$lib/components/Toast.svelte';
    import { onMount } from 'svelte';
    import ResourceTable from '$lib/components/ResourceTable.svelte';
    import ResourceForm from '$lib/components/ResourceForm.svelte';
    // Using import type tells TypeScript that we are only importing type information, not actual JavaScript code. This can sometimes help with build optimizations because the TypeScript compiler knows it can erase this import entirely when converting to JavaScript, as types don't exist at runtime.
    import type {Resource} from "$lib/types/resource";
    import {listResources} from "$lib/web/resource-service";

    // Variables declared with 'let' at the top level of <script> are 'reactive'. This means that if it's changed, Svelte will automatically re-render any parts of the HTML that depend on it.
    let resources: Resource[] = [];
    let showForm = false;

    async function loadData() {
        try {
            resources = await listResources();
        } catch (e) {
            console.error(e);
        }
    }

    // It's like the ngOnInit in Angular. Schedules a callback function after the component has been rendered.
    onMount(loadData);

    function handleUploaded(r: Resource) {
        // This creates a new array where the first element is the newly created resource . Moreover since resources is a reactive variable, Svelte will automatically update the UI to reflect the change. Why not push or unshift? Because Svelte not always detect these 'in-place' changes as a trigger to re-render the UI. In this way we make it clear that the variable has changed.
        resources = [r, ...resources];
    }

    function handleCloseForm(): void{
        showForm = false;
    }

    function openUploadForm() {
        showForm = true;
    }
</script>

<Toast />

<!-- visible, onUploaded and onClose are all properties of the ResourceForm component.
Their value is being passed from the parent component (page.svelte) to the child component (ResourceForm.svelte).
When the resourceForm calls the onUploaded function that holds that reference, it also has access to the resource array because of the closure!
The closure is a function bundled together with references to its surrounding state (lexical environment)
The function remembers the variables that were in scope where it was defined, not where it is executed!!!
So the "resources" it refers to won't be for example the same resources inside ResourceForm -->
<!--Events are how children communicate with parents. page.svelte listens for these events and decide what to do. -->
<!-- Writing this: onUploaded={handleUploaded} is just a reference to the handleUploaded function defined above!! You're not calling the function -->
<ResourceForm visible={showForm}
              onUploaded={handleUploaded}
              onClose={handleCloseForm} />

<div class="page-content-wrapper">
    <!-- {resources} is a shorthand for resources={resources}. It passes our resources array to the ResourceTable component that will have a resources property. Above we used visible={showForm} because the name was different. -->
    <ResourceTable {resources} onOpenForm={openUploadForm} />
</div>

<!--This css is scoped to this component so applied only to the HTML of this component. Svelte does this by adding unique class attributes to your elements and rewriting my CSS selectors to target those classes instead of the original selectors. -->
<style>
    .page-content-wrapper {
        max-width: 80%;
        margin-left: auto;
        margin-right: auto;
        padding: 2rem;
    }
</style>
