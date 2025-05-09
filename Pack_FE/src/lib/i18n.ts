import { addMessages, init } from 'svelte-i18n';

import it from '$lib/locales/it.json';
import en from '$lib/locales/en.json';
import es from '$lib/locales/es.json';

addMessages('it', it);
addMessages('en', en);
addMessages('es', es);

init({
    fallbackLocale: 'en',
    initialLocale: 'en'
});
