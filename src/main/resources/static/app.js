const api = {
    async request(path, options = {}) {
        const timeoutMs = options.timeout ?? 15000;
        const controller = new AbortController();
        const timer = setTimeout(() => controller.abort(), timeoutMs);
        let response;
        try {
            response = await fetch(`/api${path}`, {
                headers: { "Content-Type": "application/json", ...options.headers },
                ...options,
                signal: controller.signal
            });
        } catch (error) {
            if (error.name === "AbortError") {
                throw new Error("Сервер долго отвечает. Проверьте соединение и повторите попытку.");
            }
            throw new Error("Сервер недоступен. Проверьте подключение и попробуйте снова.");
        } finally {
            clearTimeout(timer);
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

const state = { page: location.hash.slice(1) || "mods", mods: [], authors: [], categories: [], tags: [], versions: [], loading: false, modPage: 1, pageSize: 7, entityPages: { authors: 1, categories: 1, tags: 1 }, entityPageSize: 10, sortDirection: "asc", entitySortDirections: { authors: "asc", categories: "asc", tags: "asc" }, asyncTask: null };
const labels = { mods: "Моды", authors: "Авторы", categories: "Категории", tags: "Теги" };
const esc = value => String(value == null ? "" : value).replace(/[&<>"']/g, char => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#039;" }[char]));
const list = value => Array.isArray(value) ? value : [];
function textValue(value) {
    if (typeof value === "string") return value;
    if (value == null) return "";
    return value.name || "";
}

const chipMarkup = value => `<span class="chip">${esc(value)}</span>`;
const chips = values => {
    const valuesList = list(values);
    return valuesList.length ? `<div class="chips">${valuesList.map(chipMarkup).join("")}</div>` : `<span class="muted">—</span>`;
};
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

const sortByName = (items, direction = "asc") => [...items].sort((left, right) => {
    const result = String(left.name ?? "").localeCompare(String(right.name ?? ""), "ru", { sensitivity: "base" });
    return direction === "asc" ? result : -result;
});

const relatedModsForEntity = (kind, itemName) => state.mods.filter(mod => {
    if (kind === "authors") return mod.authorName === itemName;
    if (kind === "categories") return mod.categoryName === itemName;
    return list(mod.tags).includes(itemName);
});

const getEntityRelationMarkup = (kind, itemName) => {
    const relatedMods = relatedModsForEntity(kind, itemName);
    const relation = relatedMods.length;
    if (!relation) return '<span class="muted">0 мод.</span>';
    const relatedNames = relatedMods
        .map(mod => mod.name)
        .sort((left, right) => left.localeCompare(right, "ru", { sensitivity: "base" }));
    const relatedMarkup = relatedNames.map(name => `<span>${esc(name)}</span>`).join("");
    return `<span class="relation" tabindex="0">${relation} мод.<span class="relation-popover">${relatedMarkup}</span></span>`;
};

function parseHtml(html) {
    return new DOMParser().parseFromString(html, "text/html").body;
}

function replaceHtml(element, html) {
    const parsed = parseHtml(html);
    element.replaceChildren(...parsed.childNodes);
}

function appendHtml(element, html) {
    const parsed = parseHtml(html);
    element.append(...parsed.childNodes);
}

function replaceElementHtml(element, html) {
    const parsed = parseHtml(html);
    element.replaceWith(...parsed.childNodes);
}

const pageIcon = {
    mods: "◆",
    authors: "◎",
    categories: "▦",
    tags: "◇"
};

function asyncTaskWidget() {
     if (!state.asyncTask) return "";
     return `<div class="async-widget ${state.asyncTask.done ? "done" : ""}">
         <div class="async-info">
             <span class="async-label">${esc(state.asyncTask.label || "Задача")}</span>
             <span class="async-status">${state.asyncTask.done ? '✓ Завершено' : '⟳ Выполняется...'}</span>
         </div>
         ${state.asyncTask.done ? `<button class="icon-button" data-action="close-task">×</button>` : ""}
     </div>`;
 }

function appShell(content) {
     const navigation = Object.entries(labels)
         .map(([key, label]) => `<button class="${state.page === key ? "active" : ""}" data-page="${key}">${pageIcon[key]} &nbsp;${label}</button>`)
         .join("");
     return `<div class="shell">
         <header class="topbar"><div class="brand"><div class="brand-mark">✦</div><div><h1>ModAtlas</h1><p>каталог Minecraft модов</p></div></div>${asyncTaskWidget()}
         </header>
         <div class="layout"><aside class="sidebar"><div class="nav-label">Рабочая область</div><nav class="nav">
         ${navigation}
         </nav></aside><main class="main">${content}</main></div></div>`;
 }

async function loadData() {
    state.loading = true;
    render();
    try {
        const results = await Promise.allSettled([
            api.get("/mods"),
            api.get("/authors"),
            api.get("/categories"),
            api.get("/tags")
        ]);

        const [modsResult, authorsResult, categoriesResult, tagsResult] = results;
        state.mods = modsResult.status === "fulfilled" ? modsResult.value : [];
        state.authors = authorsResult.status === "fulfilled" ? authorsResult.value : [];
        state.categories = categoriesResult.status === "fulfilled" ? categoriesResult.value : [];
        state.tags = tagsResult.status === "fulfilled" ? tagsResult.value : [];

        const versionsResult = await Promise.allSettled(state.mods.map(mod => api.get(`/mod-versions?modId=${mod.id}`)));
        state.versions = versionsResult.flatMap(result => result.status === "fulfilled" ? result.value : []);

        const failed = results.find(result => result.status === "rejected");
        if (failed) {
            throw failed.reason;
        }
    } catch (error) {
        toast(error.message, true);
    } finally {
        state.loading = false;
        render();
    }
}

function modTable(mods, withActions = true) {
    if (!mods.length) return `<div class="empty">Моды не найдены. Измените фильтры или добавьте первую запись.</div>`;
    const direction = state.sortDirection === "asc" ? "↑" : "↓";
    const actionHeader = withActions ? "<th></th>" : "";
    const rows = mods.map(mod => modTableRow(mod, withActions)).join("");
    return `<div class="table-wrap"><table><thead><tr><th><button class="sort-button" data-sort="name">НАЗВАНИЕ ${direction}</button></th><th>Автор</th><th>Категория</th><th>Теги</th><th>Версии</th>${actionHeader}</tr></thead><tbody>${rows}</tbody></table></div>`;
}

function modTableRow(mod, withActions) {
    const versions = list(mod.versions).map(version => `<div>${esc(version.versionName)} <span class="muted">· ${version.downloadCount.toLocaleString("ru-RU")}</span></div>`).join("") || '<span class="muted">—</span>';
    const actions = withActions ? `<td><div class="actions"><button class="button action-edit" data-action="edit-mod" data-id="${mod.id}">Изменить</button><button class="button danger action-delete" data-action="delete-mod" data-id="${mod.id}" data-name="${esc(mod.name)}">Удалить</button></div></td>` : "";
    return `<tr><td class="description-cell"><strong>${esc(mod.name)}</strong><br><span class="muted">${esc(mod.description)}</span></td><td>${esc(mod.authorName)}</td><td class="category-cell"><span class="chip">${esc(mod.categoryName || "Без категории")}</span></td><td>${chips(mod.tags)}</td><td class="versions-cell">${versions}</td>${actions}</tr>`;
}

function modsPage() {
     return `<div class="page-heading"><div><div class="eyebrow">Каталог</div><h2>Моды</h2></div><div style="display: flex; gap: 12px;"><button class="button" data-action="demo-async">⟳ Тест операции</button><button class="button primary" data-action="new-mod">＋ Добавить мод(ы)</button></div></div>
     <section class="panel"><div class="toolbar"><input id="search" placeholder="Поиск по названию и описанию"><input id="author-filter" list="authors-list" placeholder="Автор"><input id="category-filter" list="categories-list" placeholder="Категория"><input id="tag-filter" list="tags-list" placeholder="Тег"><button class="button" id="clear-filters">Сбросить</button></div>${catalogContent()}${dataLists()}</section>`;
 }

function dataLists() {
    const options = items => items.map(item => `<option value="${esc(item.name)}">`).join("");
    return `<datalist id="authors-list">${options(state.authors)}</datalist><datalist id="categories-list">${options(state.categories)}</datalist><datalist id="tags-list">${options(state.tags)}</datalist>`;
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
    return `<div id="catalog-content">${modTable(filtered.slice(start, start + state.pageSize))}${modPagination(pages)}</div>`;
}

function paginationItems(currentPage, totalPages) {
    if (totalPages <= 9) return Array.from({ length: totalPages }, (_, index) => index + 1);
    const items = [1];
    const start = Math.max(2, currentPage - 2);
    const end = Math.min(totalPages - 1, currentPage + 2);
    if (start > 2) items.push("ellipsis-left");
    for (let page = start; page <= end; page += 1) items.push(page);
    if (end < totalPages - 1) items.push("ellipsis-right");
    items.push(totalPages);
    return items;
}

function modPagination(pages) {
    const pageButtons = paginationItems(state.modPage, pages).map(page => {
        if (typeof page !== "number") return `<span class="pagination-ellipsis">…</span>`;
        const active = page === state.modPage ? " primary" : "";
        return `<button class="button page-number${active}" data-mod-page="${page}" ${page === state.modPage ? "disabled" : ""}>${page}</button>`;
    }).join("");
    return `<div class="pagination"><button class="button" data-page-action="prev" ${state.modPage === 1 ? "disabled" : ""}>‹ Назад</button><div class="page-numbers">${pageButtons}</div><button class="button" data-page-action="next" ${state.modPage === pages ? "disabled" : ""}>Вперед ›</button></div>`;
}

function entityPage(kind) {
    const items = state[kind];
    const title = labels[kind];
    const singular = entitySingular(kind);
    const listId = `${kind}-list`;
    const options = items.map(item => `<option value="${esc(item.name)}">`).join("");
    const searchElement = document.querySelector("#entity-search");
    const search = searchElement ? searchElement.value.toLowerCase() : "";
    const filteredItems = items.filter(item => item.name.toLowerCase().includes(search));
    const pages = Math.max(1, Math.ceil(filteredItems.length / state.entityPageSize));
    state.entityPages[kind] = Math.min(state.entityPages[kind], pages);
    const start = (state.entityPages[kind] - 1) * state.entityPageSize;
    const visibleItems = filteredItems.slice(start, start + state.entityPageSize);
    const pagination = entityPagination(kind, pages);
    return `<div class="page-heading"><div><div class="eyebrow">Справочник</div><h2>${title}</h2></div><button class="button primary" data-action="new-entity" data-kind="${kind}">＋ Добавить ${singular}</button></div>
    <section class="panel"><div class="toolbar"><input id="entity-search" list="${listId}" placeholder="Поиск по названию"></div><div id="entity-list">${entityTable(kind, visibleItems)}${pagination}</div><datalist id="${listId}">${options}</datalist></section>`;
}

function entitySingular(kind) {
    if (kind === "categories") return "категорию";
    if (kind === "authors") return "автора";
    return "тег";
}

function entityTable(kind, items) {
    if (!items.length) return `<div class="empty">Записей пока нет.</div>`;
    const direction = state.entitySortDirections[kind];
    const sortedItems = sortByName(items, direction);
    const arrow = direction === "asc" ? "↑" : "↓";
    const rows = sortedItems.map(item => entityTableRow(kind, item)).join("");
    return `<div class="table-wrap"><table><thead><tr><th>ID</th><th><button class="sort-button" data-entity-sort="${kind}">НАЗВАНИЕ ${arrow}</button></th><th>Связи</th><th></th></tr></thead><tbody>${rows}</tbody></table></div>`;
}

function entityTableRow(kind, item) {
    return `<tr><td class="muted">#${item.id}</td><td><strong>${esc(item.name)}</strong></td><td class="tag-green">${getEntityRelationMarkup(kind, item.name)}</td><td><div class="actions"><button class="button action-edit" data-action="edit-entity" data-kind="${kind}" data-id="${item.id}">Изменить</button><button class="button danger action-delete" data-action="delete-entity" data-kind="${kind}" data-id="${item.id}" data-name="${esc(item.name)}">Удалить</button></div></td></tr>`;
}

function entityPagination(kind, pages) {
    const currentPage = state.entityPages[kind];
    const pageButtons = paginationItems(currentPage, pages).map(page => {
        if (typeof page !== "number") return `<span class="pagination-ellipsis">…</span>`;
        const active = page === currentPage ? " primary" : "";
        return `<button class="button page-number${active}" data-entity-page="${page}" data-entity-kind="${kind}" ${page === currentPage ? "disabled" : ""}>${page}</button>`;
    }).join("");
    return `<div class="pagination"><button class="button" data-entity-page-action="prev" data-entity-kind="${kind}" ${currentPage === 1 ? "disabled" : ""}>‹ Назад</button><div class="page-numbers">${pageButtons}</div><button class="button" data-entity-page-action="next" data-entity-kind="${kind}" ${currentPage === pages ? "disabled" : ""}>Вперед ›</button></div>`;
}

function render() {
    let content;
    if (state.loading) {
        content = `<div class="loading">Загрузка данных…</div>`;
    } else if (state.page === "mods") {
        content = modsPage();
    } else {
        content = entityPage(state.page);
    }
    replaceHtml(document.querySelector("#app"), appShell(content) + (state.modal || ""));
    bind();
}

function bindModSort() {
    const modSortButton = document.querySelector("[data-sort='name']");
    if (!modSortButton) return;
    modSortButton.addEventListener("click", () => {
        state.sortDirection = state.sortDirection === "asc" ? "desc" : "asc";
        state.modPage = 1;
        render();
    });
}

function bindEntitySort() {
    document.querySelectorAll("[data-entity-sort]").forEach(button => {
        button.onclick = () => {
            const kind = button.dataset.entitySort;
            state.entitySortDirections[kind] = state.entitySortDirections[kind] === "asc" ? "desc" : "asc";
            state.entityPages[kind] = 1;
            render();
        };
    });
}

function bindPageActions() {
    document.querySelectorAll("[data-page-action]").forEach(button => {
        button.onclick = () => {
            state.modPage += button.dataset.pageAction === "next" ? 1 : -1;
            render();
        };
    });
    document.querySelectorAll("[data-mod-page]").forEach(button => {
        button.onclick = () => {
            state.modPage = Number(button.dataset.modPage);
            render();
        };
    });
    document.querySelectorAll("[data-entity-page]").forEach(button => {
        button.onclick = () => {
            state.entityPages[button.dataset.entityKind] = Number(button.dataset.entityPage);
            render();
        };
    });
    document.querySelectorAll("[data-entity-page-action]").forEach(button => {
        button.onclick = () => {
            const kind = button.dataset.entityKind;
            state.entityPages[kind] += button.dataset.entityPageAction === "next" ? 1 : -1;
            render();
        };
    });
}

function bindFormControls() {
    document.querySelectorAll(".add-mod-entry").forEach(button => {
        button.onclick = () => {
            const entries = document.querySelector("#mod-entries");
            if (!entries) return;
            appendHtml(entries, modEntryMarkup({}, entries.querySelectorAll(".mod-entry").length));
            renumberModEntries();
            bindFormControls();
        };
    });
    document.querySelectorAll(".remove-mod-entry").forEach(button => {
        button.onclick = () => {
            const entries = document.querySelectorAll(".mod-entry");
            if (entries.length > 1) {
                button.closest(".mod-entry").remove();
                renumberModEntries();
            }
        };
    });
    document.querySelectorAll(".add-tag").forEach(button => {
        button.onclick = () => {
            const container = button.closest(".mod-entry")?.querySelector(".tag-list") || document.querySelector("#tags");
            if (!container) return;
            appendHtml(container, `<div class="tag-row"><input name="tagName" list="tags-list" value=""><button type="button" class="button danger remove-tag">Удалить</button></div>`);
            bindTagRows();
        };
    });
    document.querySelectorAll(".add-version").forEach(button => {
        button.onclick = () => {
            const container = button.closest(".mod-entry")?.querySelector(".version-list") || document.querySelector("#versions");
            if (!container) return;
            appendHtml(container, `<div class="version-row" data-version-row><div class="field"><label>Версия</label><input name="versionName" required value=""></div><div class="field"><label>Загрузки</label><input name="downloadCount" type="number" min="0" value="0"></div><button type="button" class="button danger remove-version">Удалить</button></div>`);
            bindVersionRows();
        };
    });
    renumberModEntries();
    bindTagRows();
    bindVersionRows();
}

function renumberModEntries() {
    const entries = [...document.querySelectorAll(".mod-entry")];
    entries.forEach((entry, index) => {
        const heading = entry.querySelector(".entry-header h4");
        if (heading) heading.textContent = `Мод ${index + 1}`;
        const removeButton = entry.querySelector(".remove-mod-entry");
        if (removeButton) {
            const isSingleEntry = entries.length === 1;
            removeButton.hidden = isSingleEntry;
            removeButton.disabled = isSingleEntry;
        }
    });
}

function bindVersionRows() {
    document.querySelectorAll(".remove-version").forEach(button => {
        button.onclick = () => {
            const rows = button.closest(".mod-entry") ? button.closest(".mod-entry").querySelectorAll("[data-version-row]") : document.querySelectorAll("[data-version-row]");
            if (rows.length > 1) button.closest("[data-version-row]").remove();
        };
    });
}

function bindTagRows() {
    document.querySelectorAll(".remove-tag").forEach(button => {
        button.onclick = () => {
            const rows = button.closest(".mod-entry") ? button.closest(".mod-entry").querySelectorAll(".tag-row") : document.querySelectorAll(".tag-row");
            if (rows.length > 1) button.closest(".tag-row").remove();
        };
    });
}

function filterMods() {
    state.modPage = 1;
    replaceElementHtml(document.querySelector("#catalog-content"), catalogContent());
    bind();
}

function modal(title, body) {
    state.modal = `<div class="modal-backdrop" id="modal-backdrop"><div class="modal"><div class="modal-head"><h3>${title}</h3><button class="icon-button" data-action="close-modal">×</button></div>${body}${dataLists()}</div></div>`;
    render();
}

function modEntryMarkup(mod = {}, index = 0) {
    const versions = list(mod.versions).length ? mod.versions : [{ versionName: "1.0.0", downloadCount: 0 }];
    const versionFields = versions.map((version) => `<div class="version-row" data-version-row><div class="field"><label>Версия</label><input name="versionName" required value="${esc(version.versionName)}"></div><div class="field"><label>Загрузки</label><input name="downloadCount" type="number" min="0" value="${version.downloadCount || 0}"></div><button type="button" class="button danger remove-version" ${versions.length === 1 ? "disabled" : ""}>Удалить</button></div>`).join("");
    const tags = list(mod.tags).length ? mod.tags : [""];
    const tagFields = tags.map(tag => `<div class="tag-row"><input name="tagName" list="tags-list" value="${esc(tag)}"><button type="button" class="button danger remove-tag" ${tags.length === 1 ? "disabled" : ""}>Удалить</button></div>`).join("");
    return `<div class="mod-entry" data-mod-entry-index="${index}"><div class="entry-header"><h4>Мод ${index + 1}</h4><button type="button" class="button danger remove-mod-entry">Удалить</button></div><div class="form-grid"><div class="field"><label>Название *</label><input name="name" required maxlength="255" value="${esc(mod.name)}"></div><div class="field"><label>Автор *</label><input name="authorName" list="authors-list" required value="${esc(mod.authorName)}"></div><div class="field"><label>Категория *</label><input name="categoryName" list="categories-list" required value="${esc(mod.categoryName)}"></div><div class="field full"><label>Теги</label><div class="tag-list">${tagFields}</div><button type="button" class="button add-tag">＋ Добавить тег</button></div><div class="field full"><label>Описание *</label><textarea name="description" required maxlength="1000">${esc(mod.description)}</textarea></div><div class="field full"><label>Версии мода</label><div class="version-list">${versionFields}</div><button type="button" class="button add-version">＋ Добавить версию</button></div></div></div>`;
}

function prepareModPayload(entry) {
    const getValue = selector => entry.querySelector(selector)?.value ?? "";
    const versionNames = [...entry.querySelectorAll("input[name='versionName']")].map(field => field.value.trim());
    const downloadCounts = [...entry.querySelectorAll("input[name='downloadCount']")].map(field => Number(field.value || 0));
    const tagNames = [...entry.querySelectorAll("input[name='tagName']")].map(field => field.value.trim()).filter(Boolean);
    const normalizedVersions = versionNames.map(value => value.toLowerCase()).filter(Boolean);
    if (new Set(normalizedVersions).size !== normalizedVersions.length && normalizedVersions.length) {
        throw new Error("У одного мода не может быть одинаковых версий");
    }
    return {
        name: getValue("input[name='name']").trim(),
        description: getValue("textarea[name='description']").trim(),
        authorName: getValue("input[name='authorName']").trim(),
        categoryNames: [getValue("input[name='categoryName']").trim()].filter(Boolean),
        tagNames,
        versions: versionNames.map((versionName, index) => ({ versionName: versionName.trim(), downloadCount: Number(downloadCounts[index] || 0) })).filter(version => version.versionName)
    };
}

function modForm(mod = {}, batch = false) {
    if (batch) {
        const entries = Array.isArray(mod.entries) && mod.entries.length ? mod.entries : [{}];
        return `<form id="mod-form" novalidate data-batch="true"><div id="mod-entries">${entries.map((entry, index) => modEntryMarkup(entry, index)).join("")}</div><div class="modal-actions"><button type="button" class="button add-mod-entry">＋ Добавить мод</button><button type="button" class="button" data-action="close-modal">Отмена</button><button class="button primary">Создать моды</button></div></form>`;
    }
    const versions = list(mod.versions).length ? mod.versions : [{ versionName: "1.0.0", downloadCount: 0 }];
    const versionFields = versions.map((version) => `<div class="version-row" data-version-row><div class="field"><label>Версия</label><input name="versionName" required value="${esc(version.versionName)}"></div><div class="field"><label>Загрузки</label><input name="downloadCount" type="number" min="0" value="${version.downloadCount || 0}"></div><button type="button" class="button danger remove-version" ${versions.length === 1 ? "disabled" : ""}>Удалить</button></div>`).join("");
    const tags = list(mod.tags).length ? mod.tags : [""];
    const tagFields = tags.map(tag => `<div class="tag-row"><input name="tagName" list="tags-list" value="${esc(tag)}"><button type="button" class="button danger remove-tag" ${tags.length === 1 ? "disabled" : ""}>Удалить</button></div>`).join("");
    return `<form id="mod-form" novalidate data-id="${mod.id || ""}"><div class="form-grid"><div class="field"><label>Название *</label><input name="name" required maxlength="255" value="${esc(mod.name)}"></div><div class="field"><label>Автор *</label><input name="authorName" list="authors-list" required value="${esc(mod.authorName)}"></div><div class="field"><label>Категория *</label><input name="categoryName" list="categories-list" required value="${esc(mod.categoryName)}"></div><div class="field full"><label>Теги</label><div class="tag-list" id="tags">${tagFields}</div><button type="button" class="button add-tag">＋ Добавить тег</button></div><div class="field full"><label>Описание *</label><textarea name="description" required maxlength="1000">${esc(mod.description)}</textarea></div><div class="field full"><label>Версии мода</label><div class="version-list" id="versions">${versionFields}</div><button type="button" class="button add-version">＋ Добавить версию</button></div></div><div class="modal-actions"><button type="button" class="button" data-action="close-modal">Отмена</button><button class="button primary">${mod.id ? "Сохранить" : "Создать мод"}</button></div></form>`;
}

function entityForm(kind, item = {}) {
    const title = entityFormTitle(kind);
    return `<form id="entity-form" novalidate data-kind="${kind}" data-id="${item.id || ""}"><div class="field"><label>Название ${title}</label><input name="name" list="${kind}-list" required maxlength="255" value="${esc(item.name)}"></div><div class="modal-actions"><button type="button" class="button" data-action="close-modal">Отмена</button><button class="button primary">${item.id ? "Сохранить" : "Создать"}</button></div></form>`;
}

function entityFormTitle(kind) {
    if (kind === "categories") return "категории";
    if (kind === "authors") return "автора";
    return "тега";
}

async function startAsyncTask(delayMs, label) {
     try {
         const response = await api.save("/demo/async", { delayMs, label }, "POST");
         state.asyncTask = { taskId: response.taskId, label, done: false };
         render();
         pollAsyncTaskStatus(response.taskId);
     } catch (error) {
         toast(error.message, true);
     }
 }

 async function pollAsyncTaskStatus(taskId) {
      const interval = setInterval(async () => {
          if (!state.asyncTask || state.asyncTask.taskId !== taskId) {
              clearInterval(interval);
              return;
          }
          try {
              const status = await api.get(`/demo/async/${taskId}`);
              if (state.asyncTask) {
                  const isComplete = status.status === "COMPLETED" || status.status === "FAILED";
                  state.asyncTask.done = isComplete;
                  render();
              }
              const isDone = status.status === "COMPLETED" || status.status === "FAILED";
              if (isDone) {
                  clearInterval(interval);
                  if (status.status === "COMPLETED") {
                      toast("Задача завершена успешно");
                  } else if (status.status === "FAILED") {
                      toast("Задача завершена с ошибкой: " + (status.result || "неизвестная ошибка"), true);
                  }
              }
          } catch (error) {
              clearInterval(interval);
          }
      }, 300);
  }

async function handleAction(action, data) {
     switch (action) {
         case "close-modal":
             state.modal = "";
             render();
             return;
         case "close-task":
             state.asyncTask = null;
             render();
             return;
         case "new-mod":
             modal("Новый мод / несколько модов", modForm({}, true));
             return;
         case "edit-mod": {
             const mod = state.mods.find(item => String(item.id) === data.id);
             modal("Редактирование мода", modForm(mod));
             return;
         }
         case "delete-mod": {
             if (!confirm(`Точно удалить мод «${data.name}» и все его версии?`)) {
                 return;
             }
             await api.remove(`/mods/${data.id}`);
             await loadData();
             break;
         }
         case "new-entity":
         case "edit-entity": {
             const item = action === "edit-entity" ? state[data.kind].find(entry => String(entry.id) === data.id) : {};
             const title = action === "edit-entity" ? "Редактирование" : "Новая";
             modal(`${title} записи`, entityForm(data.kind, item));
             return;
         }
         case "delete-entity": {
             if (!confirm(`Точно удалить «${data.name}»?`)) {
                 return;
             }
             await api.remove(`/${data.kind}/${data.id}`);
             await loadData();
             break;
         }
         case "demo-async": {
             await startAsyncTask(5500, "Демо-операция");
             return;
         }
         default:
             return;
     }
 }

function validateSubmitForm(target) {
    const requiredFields = [...target.querySelectorAll("[required]")];
    const emptyField = requiredFields.find(field => !String(field.value).trim());

    if (emptyField) {
        toast("Заполните обязательные поля", true);
        emptyField.focus();
        return false;
    }

    return true;
}

async function submitModForm(target) {
    const isBatch = target.dataset.batch === "true";
    const entries = isBatch ? [...target.querySelectorAll(".mod-entry")] : [target];
    const payloads = entries.map(entry => prepareModPayload(entry));

    if (!payloads.length) {
        throw new Error("Добавьте хотя бы один мод");
    }

    const id = target.dataset.id;
    if (id) {
        await api.save(`/mods/${id}`, payloads[0], "PUT");
        return;
    }
    if (isBatch) {
        await api.save("/mods", payloads);
        return;
    }
    await api.save("/mods", [payloads[0]]);
}

async function submitEntityForm(target) {
    const form = new FormData(target);
    const kind = target.dataset.kind;
    const endpoint = `/${kind}`;
    const id = target.dataset.id;
    const rawName = form.get("name");
    const payload = { name: typeof rawName === "string" ? rawName.trim() : "" };

    await api.save(id ? `${endpoint}/${id}` : endpoint, payload, id ? "PUT" : "POST");
}

document.addEventListener("submit", async event => {
    event.preventDefault();
    const target = event.target;

    if (!validateSubmitForm(target)) {
        return;
    }

    try {
        if (target.id === "mod-form") {
            await submitModForm(target);
        } else {
            await submitEntityForm(target);
        }
        state.modal = "";
        await loadData();
        toast("Изменения сохранены");
    } catch (error) {
        toast(error.message, true);
    }
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
document.addEventListener("DOMContentLoaded", () => {
    loadData();
});

function bindSearchControls() {
    const controls = [
        document.querySelector("#search"),
        document.querySelector("#author-filter"),
        document.querySelector("#category-filter"),
        document.querySelector("#tag-filter")
    ].filter(Boolean);

    controls.forEach(element => {
        element.oninput = () => {
            state.modPage = 1;
            filterMods();
        };
        element.onchange = () => {
            state.modPage = 1;
            filterMods();
        };
    });

    const clearButton = document.querySelector("#clear-filters");
    if (clearButton) {
        clearButton.onclick = () => {
            const ids = ["#search", "#author-filter", "#category-filter", "#tag-filter"];
            ids.forEach(id => {
                const field = document.querySelector(id);
                if (field) field.value = "";
            });
            state.modPage = 1;
            filterMods();
        };
    }
}

function bind() {
    document.querySelectorAll("form").forEach(form => {
        form.noValidate = true;
    });
    document.querySelectorAll("[data-page]").forEach(button => {
        button.onclick = () => {
            location.hash = button.dataset.page;
        };
    });
    bindModSort();
    bindEntitySort();
    bindPageActions();
    document.querySelectorAll("[data-action]").forEach(button => {
        button.onclick = () => handleAction(button.dataset.action, button.dataset).catch(error => toast(error.message, true));
    });
    bindFormControls();
    bindSearchControls();
}
