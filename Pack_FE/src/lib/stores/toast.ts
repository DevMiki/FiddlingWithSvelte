import { writable } from 'svelte/store';
// This component manages the state of the toast. It acts as the single centralized place
// where the current list of active toast messages are stored. Everyone will refer to this component to know it.
// Components that trigger toasts only need to know about the showToast function, not how they are displayed for example.
// This logic can be also tested being independend of any UI rendering.
export interface Toast {
    id: number;
    message: string;
    type: 'success' | 'error';
}
// A Svelte store is an object that holds a value (or multiple values) that can be accessed and updated from different parts of your application.
// The key feature is that stores are reactive
// When the value within a store changes, any Svelte component that is "subscribed" to that store
// will automatically be notified and will re-render if its display depends on that store's value.
// Stores help you manage application state that needs to be shared across multiple components
// without having to pass data down through many levels of props (prop drilling) or emit many events up
// writable: Stores whose values can be set from "outside" using a set method or updated using an update method

// writable: This is a function imported from svelte/store. You call it to create a new writable store.
const toasts = writable<Toast[]>([]);
let counter = 0;

export function showToast(message: string, type: Toast['type'] = 'success') {
    const id = ++counter;
    // The update method is a way to modify the value of a writable store based on its current value.
    // This callback function receives the current value of the store as its parameter
    toasts.update((all) => [...all, { id, message, type }]);
    setTimeout(() => {
        toasts.update((all) => all.filter((t) => t.id !== id));
    }, 3000);
}

// export the toasts writable store so other components can import it like Toast.svelte
export default toasts;
