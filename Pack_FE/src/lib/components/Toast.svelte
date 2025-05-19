<script lang="ts">
    import toasts from '$lib/stores/toast';
</script>

<!-- $toasts is the writable store that is reactive so when someone calls showToast this reacts
This Svelte component is solely responsible for taking the array of toast objects from the $toasts
store and rendering them into the appropriate HTML structure with the correct styles
This is also a reusable UI and if you want to modify it side-wide u refer to this only. -->
<div class="toast-container">
    {#each $toasts as t (t.id)}
        <div class="toast {t.type}">
          <span class="margin-right">
            {#if t.type === 'success'}✅{:else}❌{/if}
          </span>
            {t.message}
        </div>
    {/each}
</div>

<style>
    .toast-container {
        position: relative;
        width: 100%;
        display: flex;
        justify-content: center;
        margin-bottom: 1rem;
    }

    .toast {
        display: flex;
        align-items: center;
        padding: 0.5rem 1rem;
        border-radius: 4px;
        min-width: 200px;
    }

    .toast.success {
        background-color: #4CAF50;
        color: white;
    }

    .toast.error {
        background-color: #F44336;
        color: white;
    }

    .margin-right {
        margin-right: 0.5rem;
    }
</style>