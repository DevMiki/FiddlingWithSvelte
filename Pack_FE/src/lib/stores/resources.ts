import { writable } from 'svelte/store';
import type { Resource } from '$lib/types/resource';

export const resources = writable<Resource[]>([]);
