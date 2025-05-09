<script lang="ts">
    import toasts from '$lib/stores/toast';
</script>

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
    .icon { margin-right: 0.5rem; }
</style>

<div class="toast-container">
    {#each $toasts as t (t.id)}
        <div class="toast {t.type}">
      <span class="icon">
        {#if t.type === 'success'}✅{:else}❌{/if}
      </span>
            {t.message}
        </div>
    {/each}
</div>
