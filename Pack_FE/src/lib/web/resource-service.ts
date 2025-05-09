import type {AttachmentMetadata, Resource, ResourceFormData} from '$lib/types/resource';

const API_BASE_URL = '/api/v1/resources';

export async function listResources(): Promise<Resource[]> {
    const res = await fetch(API_BASE_URL);
    if (!res.ok) throw new Error('Failed to fetch resources');
    return res.json();
}

export async function uploadResource(data: ResourceFormData): Promise<Resource> {
    const formData = new FormData();

    const resourceMetadata: Omit<ResourceFormData, 'files'> = {...data};
    delete (resourceMetadata as any).files;

    formData.append(
        'data',
        new Blob([JSON.stringify(resourceMetadata)], {type: 'application/json'})
    );

    data.files.forEach((file) => {
        formData.append('files', file, file.name);
    });

    const response = await fetch(API_BASE_URL, {
        method: 'POST',
        body: formData,
    });

    if (!response.ok) {
        const errorData = await response.json().catch(() => ({message: response.statusText}));
        throw new Error(errorData.message || 'Failed to upload resource');
    }
    return response.json();
}

export function getAttachmentDownloadUrl(attachmentId: number): string {
    return `${API_BASE_URL}/attachments/${attachmentId}/download`;
}

export function getAttachmentViewUrl(attachmentId: number): string {
    return `${API_BASE_URL}/attachments/${attachmentId}/view`;
}

export async function downloadAttachmentFile(attachmentId: number, fileName: string): Promise<void> {
    const response = await fetch(getAttachmentDownloadUrl(attachmentId));
    if (!response.ok) {
        throw new Error(`Failed to download attachment ${fileName}: ${response.statusText}`);
    }
    const blob = await response.blob();
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = fileName;
    document.body.appendChild(a);
    a.click();
    a.remove();
    window.URL.revokeObjectURL(url);
}

export async function getResourceAttachments(resourceId: number): Promise<AttachmentMetadata[]> {
    const response = await fetch(`${API_BASE_URL}/${resourceId}/attachments`);
    if (!response.ok) {
        throw new Error(`Failed to fetch attachments for resource ${resourceId}: ${response.statusText}`);
    }
    return response.json();

}
