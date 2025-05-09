package tech.thepack.web.resource.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import tech.thepack.core.resource.enums.Category;
import tech.thepack.core.resource.enums.Language;
import tech.thepack.core.resource.enums.Provider;
import tech.thepack.core.resource.enums.Role;

import java.util.Set;

public class ResourceFormDataDTO {
    @NotBlank
    @Size(max = 200)
    private String title;

    @Size(max = 1000)
    private String description;
    private Category category;
    private Language language;
    private Provider provider;
    private Set<Role> roles;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }
}