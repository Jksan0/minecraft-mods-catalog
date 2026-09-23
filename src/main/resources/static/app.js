const api = {
    async request(path, options = {}) {
        let response;
        try {
            response = await fetch(`/api${path}`, {
                headers: { "Content-Type": "application/json", ...(options.headers || {}) },
                ...options
            });
        } catch (_) {
            throw new Error("Сервер недоступен. Проверьте подключение и попробуйте снова.");
        }
        if (!response.ok) {
            let message = `Ошибка API (${response.status})`;
            try {
                const body = await response.json();
                message = russianError(body.message || body.error || message);
            } catch (_) { /* non-json error response */ }
            throw new Error(message);
        }
        return response.status === 204 ? null : response.json();
    },
    get: path => api.request(path),
    save: (path, data, method = "POST") => api.request(path, { method, body: JSON.stringify(data) }),
    remove: path => api.request(path, { method: "DELETE" })
};

const state = { page: location.hash.slice(1) || "mods", mods: [], authors: [], categories: [], tags: [], versions: [], loading: false, modPage: 1, pageSize: 7, sortDirection: "asc", entitySortDirections: { authors: "asc", categories: "asc", tags: "asc" } };
const labels = { mods: "Моды", authors: "Авторы", categories: "Категории", tags: "Теги" };
const esc = value => String(value == null ? "" : value).replace(/[&<>"']/g, char => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#039;" }[char]));
const list = value => Array.isArray(value) ? value : [];
const chips = values => list(values).length ? `<div class="chips">${list(values).map(value => `<span class="chip">${esc(value)}</span>`).join("")}</div>` : `<span class="muted">—</span>`;
const russianError = message => ({
    "Internal server error": "Внутренняя ошибка сервера",
    "Validation failed": "Проверьте введенные данные",
    "Malformed request body": "Некорректный формат запроса",
    "Request body is required": "Заполните данные запроса",
    "At least one mod version is required": "Добавьте хотя бы одну версию мода"
}[message] || message
    .replace(/^Mod not found:/, "Мод не найден:")
    .replace(/^Author not found:/, "Автор не найден:")
    .replace(/^Category not found:/, "Категория не найдена:")
    .replace(/^Tag not found:/, "Тег не найден:")
    .replace(/^Mod version not found:/, "Версия мода не найдена:")
    .replace(/^Mod name already exists:/, "Мод с таким названием уже существует:")
    .replace(/^Author name already exists:/, "Автор с таким именем уже существует:")
    .replace(/^Category name already exists:/, "Категория с таким названием уже существует:")
    .replace(/^Tag name already exists:/, "Тег с таким названием уже существует:")
    .replace(/^Version name is required$/, "Укажите название версии")
    .replace(/^downloadCount must be non-negative$/, "Количество загрузок не может быть отрицательным")
    .replace(/^Version downloadCount must be non-negative$/, "Количество загрузок версии не может быть отрицательным")
    .replace(/^modId is required$/, "Укажите мод")
    .replace(/^Author name is required$/, "Укажите имя автора")
    .replace(/^Category name is required$/, "Укажите название категории")
    .replace(/^Tag name is required$/, "Укажите название тега")
    .replace(/^Category is required$/, "Укажите категорию")
    .replace(/^Unknown task id:/, "Неизвестный идентификатор задачи:")
    .replace(/^threadCount and incrementPerThread must be positive$/, "Количество потоков и итераций должно быть положительным")
    .replace(/^Name is required$/, "Укажите название")
    .replace(/^Description is required$/, "Укажите описание")
    .replace(/^Bad Request$/, "Некорректный запрос")
    .replace(/^Not Found$/, "Запись не найдена")
    .replace(/^Conflict$/, "Операция конфликтует с существующими данными"));

function appShell(content) {
    return `<div class="shell">
        <header class="topbar"><div class="brand"><div class="brand-mark">✦</div><div><h1>ModAtlas</h1><p>каталог Minecraft модов</p></div></div>
        </header>
        <div class="layout"><aside class="sidebar"><div class="nav-label">Рабочая область</div><nav class="nav">
        ${Object.entries(labels).map(([key, label]) => `<button class="${state.page === key ? "active" : ""}" data-page="${key}">${key === "mods" ? "◆" : key === "authors" ? "◎" : key === "categories" ? "▦" : "◇"} &nbsp;${label}</button>`).join("")}
        </nav></aside><main class="main">${content}</main></div></div>`;
}

async function loadData() {
    state.loading = true;
    render();
    try {
        const [mods, authors, categories, tags] = await Promise.all([api.get("/mods"), api.get("/authors"), api.get("/categories"), api.get("/tags")]);
        state.mods = mods; state.authors = authors; state.categories = categories; state.tags = tags;
        state.versions = (await Promise.all(mods.map(mod => api.get(`/mod-versions?modId=${mod.id}`)))).flat();
    } catch (error) { toast(error.message, true); }
    state.loading = false;
    render();
}

function modTable(mods, withActions = true) {
    if (!mods.length) return `<div class="empty">Моды не найдены. Измените фильтры или добавьте первую запись.</div>`;
    return `<div class="table-wrap"><table><thead><tr><th><button class="sort-button" data-sort="name">НАЗВАНИЕ ${state.sortDirection === "asc" ? "↑" : "↓"}</button></th><th>Автор</th><th>Категория</th><th>Теги</th><th>Версии</th>${withActions ? "<th></th>" : ""}</tr></thead><tbody>${mods.map(mod => `<tr><td class="description-cell"><strong>${esc(mod.name)}</strong><br><span class="muted">${esc(mod.description)}</span></td><td>${esc(mod.authorName)}</td><td class="category-cell"><span class="chip">${esc(mod.categoryName || "Без категории")}</span></td><td>${chips(mod.tags)}</td><td class="versions-cell">${list(mod.versions).map(version => `<div>${esc(version.versionName)} <span class="muted">· ${version.downloadCount.toLocaleString("ru-RU")}</span></div>`).join("") || '<span class="muted">—</span>'}</td>${withActions ? `<td><div class="actions"><button class="button action-edit" data-action="edit-mod" data-id="${mod.id}">Изменить</button><button class="button danger action-delete" data-action="delete-mod" data-id="${mod.id}" data-name="${esc(mod.name)}">Удалить</button></div></td>` : ""}</tr>`).join("")}</tbody></table></div>`;
}

function modsPage() {
    return `<div class="page-heading"><div><div class="eyebrow">Каталог</div><h2>Моды</h2></div><button class="button primary" data-action="new-mod">＋ Добавить мод</button></div>
    <section class="panel"><div class="toolbar"><input id="search" placeholder="Поиск по названию и описанию"><input id="author-filter" list="authors-list" placeholder="Автор"><input id="category-filter" list="categories-list" placeholder="Категория"><input id="tag-filter" list="tags-list" placeholder="Тег"><button class="button" id="clear-filters">Сбросить</button></div>${catalogContent()}${dataLists()}</section>`;
}

function dataLists() {
    return `<datalist id="authors-list">${state.authors.map(item => `<option value="${esc(item.name)}">`).join("")}</datalist><datalist id="categories-list">${state.categories.map(item => `<option value="${esc(item.name)}">`).join("")}</datalist><datalist id="tags-list">${state.tags.map(item => `<option value="${esc(item.name)}">`).join("")}</datalist>`;
}

function catalogContent() {
    const searchElement = document.querySelector("#search");
    const authorElement = document.querySelector("#author-filter");
    const categoryElement = document.querySelector("#category-filter");
    const tagElement = document.querySelector("#tag-filter");
    const search = searchElement ? searchElement.value.toLowerCase() : "";
    const author = authorElement ? authorElement.value.toLowerCase() : "";
    const category = categoryElement ? categoryElement.value.toLowerCase() : "";
    const tag = tagElement ? tagElement.value.toLowerCase() : "";
    const filtered = state.mods.filter(mod => `${mod.name} ${mod.description}`.toLowerCase().includes(search) &&
        (!author || (mod.authorName || "").toLowerCase().includes(author)) &&
        (!category || (mod.categoryName || "").toLowerCase().includes(category)) &&
        (!tag || list(mod.tags).some(value => value.toLowerCase().includes(tag))));
    filtered.sort((left, right) => {
        const result = left.name.localeCompare(right.name, "ru", { sensitivity: "base" });
        return state.sortDirection === "asc" ? result : -result;
    });
    const pages = Math.max(1, Math.ceil(filtered.length / state.pageSize));
    state.modPage = Math.min(state.modPage, pages);
    const start = (state.modPage - 1) * state.pageSize;
    return `<div id="catalog-content">${modTable(filtered.slice(start, start + state.pageSize))}<div class="pagination"><button class="button" data-page-action="prev" ${state.modPage === 1 ? "disabled" : ""}>‹ Назад</button><span>Страница ${state.modPage} из ${pages}</span><button class="button" data-page-action="next" ${state.modPage === pages ? "disabled" : ""}>Вперед ›</button></div></div>`;
}

function entityPage(kind) {
    const items = state[kind];
    const title = labels[kind];
    const singular = kind === "categories" ? "категорию" : kind === "authors" ? "автора" : "тег";
    const listId = `${kind}-list`;
    return `<div class="page-heading"><div><div class="eyebrow">Справочник</div><h2>${title}</h2></div><button class="button primary" data-action="new-entity" data-kind="${kind}">＋ Добавить ${singular}</button></div>
    <section class="panel"><div class="toolbar"><input id="entity-search" list="${listId}" placeholder="Поиск по названию"></div><div id="entity-list">${entityTable(kind, items)}</div><datalist id="${listId}">${items.map(item => `<option value="${esc(item.name)}">`).join("")}</datalist></section>`;
}

function entityTable(kind, items) {
    if (!items.length) return `<div class="empty">Записей пока нет.</div>`;
    const direction = state.entitySortDirections[kind];
    const sortedItems = [...items].sort((left, right) => {
        const result = left.name.localeCompare(right.name, "ru", { sensitivity: "base" });
        return direction === "asc" ? result : -result;
    });
    return `<div class="table-wrap"><table><thead><tr><th>ID</th><th><button class="sort-button" data-entity-sort="${kind}">НАЗВАНИЕ ${direction === "asc" ? "↑" : "↓"}</button></th><th>Связи</th><th></th></tr></thead><tbody>${sortedItems.map(item => {
        const relation = kind === "authors" ? state.mods.filter(mod => mod.authorName === item.name).length : kind === "categories" ? state.mods.filter(mod => mod.categoryName === item.name).length : state.mods.filter(mod => list(mod.tags).includes(item.name)).length;
        const relatedMods = state.mods.filter(mod => kind === "authors" ? mod.authorName === item.name : kind === "categories" ? mod.categoryName === item.name : list(mod.tags).includes(item.name));
        const relatedNames = relatedMods.map(mod => mod.name).sort((left, right) => left.localeCompare(right, "ru", { sensitivity: "base" }));
        const relationMarkup = relation ? `<span class="relation" tabindex="0">${relation} мод.<span class="relation-popover">${relatedNames.map(name => `<span>${esc(name)}</span>`).join("")}</span></span>` : `<span class="muted">0 мод.</span>`;
        return `<tr><td class="muted">#${item.id}</td><td><strong>${esc(item.name)}</strong></td><td class="tag-green">${relationMarkup}</td><td><div class="actions"><button class="button action-edit" data-action="edit-entity" data-kind="${kind}" data-id="${item.id}">Изменить</button><button class="button danger action-delete" data-action="delete-entity" data-kind="${kind}" data-id="${item.id}" data-name="${esc(item.name)}">Удалить</button></div></td></tr>`;
    }).join("")}</tbody></table></div>`;
}

function render() {
    const content = state.loading ? `<div class="loading">Загрузка данных…</div>` : state.page === "mods" ? modsPage() : entityPage(state.page);
    document.querySelector("#app").innerHTML = appShell(content) + (state.modal || "");
    bind();
}

function bind() {
    document.querySelectorAll("form").forEach(form => { form.noValidate = true; });
    document.querySelectorAll("[data-page]").forEach(button => button.onclick = () => { location.hash = button.dataset.page; });
    const modSortButton = document.querySelector("[data-sort='name']");
    if (modSortButton) modSortButton.addEventListener("click", () => {
        state.sortDirection = state.sortDirection === "asc" ? "desc" : "asc";
        state.modPage = 1;
        render();
    });
    document.querySelectorAll("[data-entity-sort]").forEach(button => button.onclick = () => {
        const kind = button.dataset.entitySort;
        state.entitySortDirections[kind] = state.entitySortDirections[kind] === "asc" ? "desc" : "asc";
        render();
    });
    document.querySelectorAll("[data-page-action]").forEach(button => button.onclick = () => { state.modPage += button.dataset.pageAction === "next" ? 1 : -1; render(); });
    document.querySelectorAll("[data-action]").forEach(button => button.onclick = () => handleAction(button.dataset.action, button.dataset).catch(error => toast(error.message, true)));
    const addTag = document.querySelector("#add-tag");
    if (addTag) addTag.onclick = () => {
        const tags = document.querySelector("#tags");
        tags.insertAdjacentHTML("beforeend", `<div class="tag-row"><input name="tagName" list="tags-list" value=""><button type="button" class="button danger remove-tag">Удалить</button></div>`);
        bindTagRows();
    };
    bindTagRows();
    const addVersion = document.querySelector("#add-version");
    if (addVersion) addVersion.onclick = () => {
        const versions = document.querySelector("#versions");
        const index = versions.querySelectorAll("[data-version-row]").length + 1;
        versions.insertAdjacentHTML("beforeend", `<div class="version-row" data-version-row><div class="field"><label>Версия ${index}</label><input name="versionName" required value=""></div><div class="field"><label>Загрузки</label><input name="downloadCount" type="number" min="0" value="0"></div><button type="button" class="button danger remove-version">Удалить</button></div>`);
        bindVersionRows();
    };
    bindVersionRows();
    const search = document.querySelector("#search");
    if (search) ["search", "author-filter", "category-filter", "tag-filter"].forEach(id => document.querySelector(`#${id}`).oninput = filterMods);
    const entitySearch = document.querySelector("#entity-search");
    if (entitySearch) entitySearch.oninput = () => {
        const items = state[state.page].filter(item => item.name.toLowerCase().includes(entitySearch.value.toLowerCase()));
        document.querySelector("#entity-list").innerHTML = entityTable(state.page, items);
        bind();
    };
    const clear = document.querySelector("#clear-filters");
    if (clear) clear.onclick = () => { ["search", "author-filter", "category-filter", "tag-filter"].forEach(id => document.querySelector(`#${id}`).value = ""); filterMods(); };
}

function bindVersionRows() {
    document.querySelectorAll(".remove-version").forEach(button => button.onclick = () => {
        const rows = document.querySelectorAll("[data-version-row]");
        if (rows.length > 1) button.closest("[data-version-row]").remove();
    });
}

function bindTagRows() {
    document.querySelectorAll(".remove-tag").forEach(button => button.onclick = () => {
        const rows = document.querySelectorAll(".tag-row");
        if (rows.length > 1) button.closest(".tag-row").remove();
    });
}

function filterMods() {
    state.modPage = 1;
    document.querySelector("#catalog-content").outerHTML = catalogContent();
    bind();
}

function modal(title, body) {
    state.modal = `<div class="modal-backdrop" id="modal-backdrop"><div class="modal"><div class="modal-head"><h3>${title}</h3><button class="icon-button" data-action="close-modal">×</button></div>${body}${dataLists()}</div></div>`;
    render();
}

function modForm(mod = {}) {
    const versions = list(mod.versions).length ? mod.versions : [{ versionName: "1.0.0", downloadCount: 0 }];
    const versionFields = versions.map((version, index) => `<div class="version-row" data-version-row><div class="field"><label>Версия ${index + 1}</label><input name="versionName" required value="${esc(version.versionName)}"></div><div class="field"><label>Загрузки</label><input name="downloadCount" type="number" min="0" value="${version.downloadCount || 0}"></div><button type="button" class="button danger remove-version" ${versions.length === 1 ? "disabled" : ""}>Удалить</button></div>`).join("");
    const tags = list(mod.tags).length ? mod.tags : [""];
    const tagFields = tags.map(tag => `<div class="tag-row"><input name="tagName" list="tags-list" value="${esc(tag)}"><button type="button" class="button danger remove-tag" ${tags.length === 1 ? "disabled" : ""}>Удалить</button></div>`).join("");
    return `<form id="mod-form" novalidate data-id="${mod.id || ""}"><div class="form-grid"><div class="field"><label>Название</label><input name="name" required maxlength="255" value="${esc(mod.name)}"></div><div class="field"><label>Автор</label><input name="authorName" list="authors-list" required value="${esc(mod.authorName)}"></div><div class="field"><label>Категория</label><input name="categoryName" list="categories-list" value="${esc(mod.categoryName)}"></div><div class="field full"><label>Теги</label><div id="tags">${tagFields}</div><button type="button" class="button" id="add-tag">＋ Добавить тег</button></div><div class="field full"><label>Описание</label><textarea name="description" required maxlength="1000">${esc(mod.description)}</textarea></div><div class="field full"><label>Версии мода</label><div id="versions">${versionFields}</div><button type="button" class="button" id="add-version">＋ Добавить версию</button></div></div><div class="modal-actions"><button type="button" class="button" data-action="close-modal">Отмена</button><button class="button primary">${mod.id ? "Сохранить" : "Создать мод"}</button></div></form>`;
}

function entityForm(kind, item = {}) {
    const title = kind === "categories" ? "категории" : kind === "authors" ? "автора" : "тега";
    return `<form id="entity-form" novalidate data-kind="${kind}" data-id="${item.id || ""}"><div class="field"><label>Название ${title}</label><input name="name" list="${kind}-list" required maxlength="255" value="${esc(item.name)}"></div><div class="modal-actions"><button type="button" class="button" data-action="close-modal">Отмена</button><button class="button primary">${item.id ? "Сохранить" : "Создать"}</button></div></form>`;
}

async function handleAction(action, data) {
    if (action === "close-modal") { state.modal = ""; render(); return; }
    if (action === "new-mod") { modal("Новый мод", modForm()); return; }
    if (action === "edit-mod") { modal("Редактирование мода", modForm(state.mods.find(mod => String(mod.id) === data.id))); return; }
    if (action === "delete-mod" && confirm(`Точно удалить мод «${data.name}» и все его версии?`)) { await api.remove(`/mods/${data.id}`); await loadData(); return; }
    if (action === "new-entity" || action === "edit-entity") {
        const item = action === "edit-entity" ? state[data.kind].find(entry => String(entry.id) === data.id) : {};
        modal(`${action === "edit-entity" ? "Редактирование" : "Новая"} записи`, entityForm(data.kind, item)); return;
    }
    if (action === "delete-entity" && confirm(`Точно удалить «${data.name}»?`)) {
        await api.remove(`/${data.kind}/${data.id}`); await loadData();
    }
}

document.addEventListener("submit", async event => {
    event.preventDefault();
    try {
        const form = new FormData(event.target);
        const requiredFields = [...event.target.querySelectorAll("[required]")];
        const emptyField = requiredFields.find(field => !String(field.value).trim());
        if (emptyField) {
            toast("Заполните обязательные поля", true);
            emptyField.focus();
            return;
        }
        if (event.target.id === "mod-form") {
            const id = event.target.dataset.id;
            const versionNames = form.getAll("versionName");
            const downloadCounts = form.getAll("downloadCount");
            const normalizedVersions = versionNames.map(name => String(name).trim().toLowerCase()).filter(Boolean);
            if (new Set(normalizedVersions).size !== normalizedVersions.length) {
                toast("У одного мода не может быть одинаковых версий", true);
                return;
            }
            const tagNames = form.getAll("tagName").map(tag => String(tag).trim()).filter(Boolean);
            const payload = { name: form.get("name"), description: form.get("description"), authorName: form.get("authorName"), categoryNames: [form.get("categoryName")].filter(Boolean), tagNames, versions: versionNames.map((versionName, index) => ({ versionName, downloadCount: Number(downloadCounts[index] || 0) })) };
            if (id) await api.save(`/mods/${id}`, payload, "PUT"); else await api.save("/mods", [payload]);
        } else {
            const kind = event.target.dataset.kind;
            const endpoint = `/${kind}`;
            const id = event.target.dataset.id;
            await api.save(id ? `${endpoint}/${id}` : endpoint, { name: form.get("name") }, id ? "PUT" : "POST");
        }
        state.modal = ""; await loadData(); toast("Изменения сохранены");
    } catch (error) { toast(error.message, true); }
});

function toast(message, error = false) {
    const previousToast = document.querySelector(".toast");
    if (previousToast) previousToast.remove();
    const node = document.createElement("div"); node.className = `toast${error ? " error" : ""}`; node.textContent = message; document.body.append(node);
    setTimeout(() => node.remove(), 3500);
}

window.addEventListener("hashchange", () => {
    const page = location.hash.slice(1);
    state.page = page === "dashboard" ? "mods" : page || "mods";
    state.modal = "";
    render();
});
loadData();
