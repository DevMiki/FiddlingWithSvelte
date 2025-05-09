import { writable } from 'svelte/store';

export interface Toast {
    id: number;
    message: string;
    type: 'success' | 'error';
}

const toasts = writable<Toast[]>([]);
let counter = 0;

export function showToast(message: string, type: Toast['type'] = 'success') {
    const id = ++counter;
    toasts.update((all) => [...all, { id, message, type }]);
    setTimeout(() => {
        toasts.update((all) => all.filter((t) => t.id !== id));
    }, 3000);
}

export default toasts;
