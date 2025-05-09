export type Category = 'Leadership' | 'Managing_Complexity';
export type Language = 'en' | 'it' | 'es';
export type Provider = 'Skilla' | 'Linkedin' | 'Pack' | 'Mentor';
export type Role = 'Mentor/Coach' | 'Mentee/Coachee';

export interface Resource {
    id: number;
    title: string;
    description?: string;
    category: Category;
    language: Language;
    provider: Provider;
    roles: Role[];
    attachmentCount: number;
}

export interface ResourceFormDataCore {
    title: string;
    description: string;
    category: Category;
    language: Language;
    provider: Provider;
    roles: Role[];
}

export interface AttachmentMetadata {
    id: number;
    fileName: string;
    fileType: string;
    fileSize: number;
    uploadedAt: string;
}

export interface ResourceFormData extends ResourceFormDataCore {
    files: File[];
}
