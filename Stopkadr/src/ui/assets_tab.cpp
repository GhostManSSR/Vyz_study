#include "ui/assets_tab.h"
#include "ui/theme.h"
#include "ui/dialogs.h"
#include "ui/dialogs_lab8.h"
#include <FL/fl_ask.H>
#include <algorithm>
#include <exception>
#include <unordered_map>

namespace {
    template<typename F> bool runSafely(F&& f) {
        try { f(); return true; }
        catch (const std::exception& e) { fl_alert("Ошибка: %s", e.what()); return false; }
    }
}

AssetsTab::AssetsTab(int X, int Y, int W, int H, Service& svc)
    : Fl_Group(X, Y, W, H), svc_(svc) {
    box(FL_FLAT_BOX);
    color(Theme::bg);

    filter_ = new Fl_Choice(0, 0, 160, Theme::BTN_H);
    filter_->add("Все|В стойке|На съёмке");
    filter_->value(0);
    filter_->callback(filterCb, this);

    addBtn_ = Theme::primary(0, 0, 120, 40, "+ Техника");
    editBtn_ = Theme::secondary(0, 0, 120, 40, "Изменить");
    copyBtn_ = Theme::secondary(0, 0, 120, 40, "Копировать");
    deleteBtn_ = Theme::dangerBtn(0, 0, 120, 40, "Удалить");
    addBtn_->callback(addCb, this);
    editBtn_->callback(editCb, this);
    copyBtn_->callback(copyCb, this);
    deleteBtn_->callback(deleteCb, this);

    issueBtn_ = Theme::primary(0, 0, 120, 40, "Выдать");
    returnBtn_ = Theme::secondary(0, 0, 120, 40, "Принять");
    bulkBtn_ = Theme::primary(0, 0, 164, 40, "Выдать несколько");
    reassignBtn_ = Theme::secondary(0, 0, 125, 40, "Сменить");
    searchBtn_ = Theme::secondary(0, 0, 120, 40, "Поиск");
    historyBtn_ = Theme::secondary(0, 0, 120, 40, "История");
    issueBtn_->callback(issueCb, this);
    returnBtn_->callback(returnCb, this);
    bulkBtn_->callback(bulkCb, this);
    reassignBtn_->callback(reassignCb, this);
    searchBtn_->callback(searchCb, this);
    historyBtn_->callback(historyCb, this);

    prevBtn_ = Theme::secondary(0, 0, 48, 32, "◀");
    nextBtn_ = Theme::secondary(0, 0, 48, 32, "▶");
    prevBtn_->callback(prevCb, this);
    nextBtn_->callback(nextCb, this);
    pageLabel_ = new Fl_Box(0, 0, 210, 32, "Стр. 1");

    table_ = new DataTable(0, 0, 100, 100);
    table_->setup({"ID", "Инв.№", "Модель", "Категория", "Статус", "У кого"},
                  {1, 2, 5, 3, 2, 4});
    table_->setBadgeColumn(4, {
        {"В стойке", Theme::success},
        {"На съёмке", Theme::accent}
    });
    table_->onActivate([this](long id) { doEdit(id); });
    table_->when(FL_WHEN_CHANGED);
    table_->callback([](Fl_Widget*, void* user) {
        static_cast<AssetsTab*>(user)->updateButtons();
    }, this);

    end();
    layout();
    reload();
}

void AssetsTab::layout() {
    const int p = Theme::PAD;
    const int gap = Theme::GAP;
    const int bh = Theme::BTN_H;
    filter_->resize(x() + p, y() + p, 160, bh);

    int right = x() + w() - p;
    for (Fl_Button* btn : {deleteBtn_, copyBtn_, editBtn_, addBtn_}) {
        right -= 120;
        btn->resize(right, y() + p, 120, bh);
        right -= gap;
    }

    int actionY = y() + p + bh + 12;
    int ax = x() + p;
    for (auto item : {std::pair<Fl_Button*,int>{issueBtn_,120}, {returnBtn_,120},
                      {bulkBtn_,164}, {reassignBtn_,125}, {searchBtn_,120}, {historyBtn_,120}}) {
        item.first->resize(ax, actionY, item.second, bh);
        ax += item.second + gap;
    }
    int pageY = actionY + bh + 12;
    prevBtn_->resize(x() + p, pageY, 48, 32);
    pageLabel_->resize(x() + p + 58, pageY, 200, 32);
    nextBtn_->resize(x() + p + 268, pageY, 48, 32);
    int tableY = pageY + 42;
    int tableH = std::max(80, h() - (tableY - y()) - p);
    table_->resize(x() + p, tableY, w() - 2 * p, tableH);
}

void AssetsTab::resize(int X, int Y, int W, int H) {
    Fl_Group::resize(X, Y, W, H);
    layout();
}

void AssetsTab::reload() {
    runSafely([&] {
        current_.clear();
        const int filter = filter_->value();
        if (!searchMode_) {
            const std::string status = filter == 1 ? "В стойке" :
                                       filter == 2 ? "На съёмке" : "";
            auto paged = svc_.listAssetsPaged(static_cast<int>(page_), 20, status);
            if (page_ > 0 && paged.items.empty()) {
                --page_;
                paged = svc_.listAssetsPaged(static_cast<int>(page_), 20, status);
            }
            current_ = std::move(paged.items);
            hasNext_ = paged.hasNext;
        } else {
            const bool freeOnly = searchFree_ || filter == 1;
            auto assets = svc_.searchAssets(searchCategory_, freeOnly, searchModel_);
            if (filter == 2) {
                assets.erase(std::remove_if(assets.begin(), assets.end(),
                             [](const Asset& a) { return !a.isAssigned(); }),
                             assets.end());
            }
            total_ = assets.size();
            const std::size_t pages = (total_ + 19) / 20;
            if (pages > 0 && page_ >= pages) page_ = pages - 1;
            if (pages == 0) page_ = 0;
            const std::size_t start = std::min(page_ * 20, total_);
            const std::size_t stop = std::min(start + 20, total_);
            hasNext_ = (page_ + 1) * 20 < total_;
            std::unordered_map<long, std::string> holders;
            for (const auto& entry : svc_.assets())
                holders[entry.asset.id()] = entry.holder;
            for (std::size_t i = start; i < stop; ++i)
                current_.push_back({assets[i], holders[assets[i].id()]});
        }
        std::vector<std::vector<std::string>> data;
        std::vector<long> ids;
        for (const auto& row : current_) {
            const auto& a = row.asset;
            data.push_back({std::to_string(a.id()), a.inventoryNumber(), a.model(),
                            a.category(), a.isAssigned() ? "На съёмке" : "В стойке",
                            row.holder.empty() ? "—" : row.holder});
            ids.push_back(a.id());
        }
        table_->setRows(data, ids);
        const std::string text = "Стр. " + std::to_string(page_ + 1) +
                                 (hasNext_ ? " (есть следующая)" : "");
        pageLabel_->copy_label(text.c_str());
        if (page_ > 0) prevBtn_->activate(); else prevBtn_->deactivate();
        if (hasNext_) nextBtn_->activate(); else nextBtn_->deactivate();
        updateButtons();
    });
}

void AssetsTab::updateButtons() {
    const long id = table_->selectedId();
    auto it = std::find_if(current_.begin(), current_.end(),
                           [id](const AssetRow& row) { return row.asset.id() == id; });
    const bool selected = it != current_.end();
    const bool assigned = selected && it->asset.isAssigned();
    for (auto* b : {editBtn_, copyBtn_, historyBtn_})
        if (selected) b->activate(); else b->deactivate();
    if (selected && !assigned) {
        issueBtn_->activate(); returnBtn_->deactivate();
        reassignBtn_->deactivate(); deleteBtn_->activate();
    } else if (selected && assigned) {
        issueBtn_->deactivate(); returnBtn_->activate();
        reassignBtn_->activate(); deleteBtn_->activate();
    } else {
        issueBtn_->deactivate(); returnBtn_->deactivate();
        reassignBtn_->deactivate(); deleteBtn_->deactivate();
    }
}

void AssetsTab::doAdd() {
    AssetDialog d([this](auto inv, auto model, auto cat) {
        runSafely([&] { svc_.addAsset(inv, model, cat); page_ = 0; reload(); });
    });
    d.show(); while (d.shown()) Fl::wait();
}
void AssetsTab::doEdit(long id) {
    for (const auto& row : current_) {
        if (row.asset.id() != id) continue;
        AssetDialog d([](auto, auto, auto) {}, row.asset.inventoryNumber(),
                      row.asset.model(), row.asset.category());
        d.show(); while (d.shown()) Fl::wait();
        return;
    }
}
void AssetsTab::doIssue(long) {
    runSafely([&] {
        std::vector<std::pair<long, std::string>> employees, freeAssets;
        for (const auto& e : svc_.activeEmployees())
            employees.push_back({e.id(), e.fullName()});
        for (const auto& a : svc_.freeAssets())
            freeAssets.push_back({a.id(), a.inventoryNumber() + " — " + a.model()});
        IssueDialog d([this](long eid, long aid) {
            runSafely([&] { svc_.issue(aid, eid); reload(); });
        }, employees, freeAssets);
        d.show(); while (d.shown()) Fl::wait();
    });
}
void AssetsTab::doReturn(long id) {
    if (fl_choice("Принять технику обратно в стойку?", "Отмена", "Принять", nullptr) != 1)
        return;
    runSafely([&] { svc_.giveBack(id); reload(); });
}
void AssetsTab::doCopy(long id) {
    auto it = std::find_if(current_.begin(), current_.end(),
                           [id](const AssetRow& r) { return r.asset.id() == id; });
    if (it == current_.end()) return;
    AssetDialog d([this, id](auto inv, auto, auto) {
        runSafely([&] { svc_.duplicateAsset(id, inv); page_ = 0; reload(); });
    }, "", it->asset.model(), it->asset.category(), true);
    d.show(); while (d.shown()) Fl::wait();
}
void AssetsTab::doBulk() {
    runSafely([&] {
        std::vector<std::pair<long, std::string>> employees, assets;
        for (const auto& e : svc_.activeEmployees())
            employees.push_back({e.id(), e.fullName()});
        // Show all assets here: an already-issued item can be selected
        // deliberately to demonstrate the all-or-nothing rollback test.
        for (const auto& row : svc_.assets()) {
            const auto& a = row.asset;
            assets.push_back({a.id(), a.inventoryNumber() + " — " + a.model() +
                              (a.isAssigned() ? " [занята]" : "")});
        }
        BulkIssueDialog d([this](const std::vector<long>& ids, long employeeId) {
            return runSafely([&] { svc_.issueMultiple(ids, employeeId); reload(); });
        }, employees, assets);
        d.show(); while (d.shown()) Fl::wait();
    });
}
void AssetsTab::doSearch() {
    SearchDialog d([this](const std::string& cat, bool free, const std::string& model) {
        searchCategory_ = cat;
        searchFree_ = free;
        searchModel_ = model;
        searchMode_ = (cat != "Все" || free || !model.empty());
        page_ = 0;
        reload();
    });
    d.show(); while (d.shown()) Fl::wait();
}
void AssetsTab::doHistory(long id) {
    runSafely([&] {
        auto it = std::find_if(current_.begin(), current_.end(),
                               [id](const AssetRow& r) { return r.asset.id() == id; });
        const std::string inv = it == current_.end() ? std::to_string(id)
                                                          : it->asset.inventoryNumber();
        HistoryDialog d(inv, svc_.assetHistory(id));
        d.show(); while (d.shown()) Fl::wait();
    });
}
void AssetsTab::doReassign(long id) {
    runSafely([&] {
        auto it = std::find_if(current_.begin(), current_.end(),
                               [id](const AssetRow& r) { return r.asset.id() == id; });
        if (it == current_.end()) return;
        std::vector<std::pair<long, std::string>> employees;
        for (const auto& e : svc_.activeEmployees())
            employees.push_back({e.id(), e.fullName()});
        ReassignDialog d([this, id](long employeeId) {
            return runSafely([&] { svc_.reassignAsset(id, employeeId); reload(); });
        }, it->holder, employees);
        d.show(); while (d.shown()) Fl::wait();
    });
}
void AssetsTab::doDelete(long id) {
    if (fl_choice("Удалить технику и всю историю её выдач?", "Отмена", "Удалить", nullptr) != 1)
        return;
    runSafely([&] { svc_.deleteAssetCascade(id); reload(); });
}

long AssetsTab::requireSelection(AssetsTab* t) {
    long id = t->table_->selectedId();
    if (id < 0) fl_alert("Выберите технику в таблице");
    return id;
}
void AssetsTab::filterCb(Fl_Widget*, void* v) {
    auto* t = static_cast<AssetsTab*>(v);
    t->page_ = 0; t->reload();
}
void AssetsTab::addCb(Fl_Widget*, void* v) { static_cast<AssetsTab*>(v)->doAdd(); }
void AssetsTab::editCb(Fl_Widget*, void* v) {
    auto* t = static_cast<AssetsTab*>(v); long id = requireSelection(t);
    if (id >= 0) t->doEdit(id);
}
void AssetsTab::issueCb(Fl_Widget*, void* v) {
    auto* t = static_cast<AssetsTab*>(v); long id = requireSelection(t);
    if (id >= 0) t->doIssue(id);
}
void AssetsTab::returnCb(Fl_Widget*, void* v) {
    auto* t = static_cast<AssetsTab*>(v); long id = requireSelection(t);
    if (id >= 0) t->doReturn(id);
}
void AssetsTab::copyCb(Fl_Widget*, void* v) {
    auto* t = static_cast<AssetsTab*>(v); long id = requireSelection(t);
    if (id >= 0) t->doCopy(id);
}
void AssetsTab::bulkCb(Fl_Widget*, void* v) { static_cast<AssetsTab*>(v)->doBulk(); }
void AssetsTab::searchCb(Fl_Widget*, void* v) { static_cast<AssetsTab*>(v)->doSearch(); }
void AssetsTab::historyCb(Fl_Widget*, void* v) {
    auto* t = static_cast<AssetsTab*>(v); long id = requireSelection(t);
    if (id >= 0) t->doHistory(id);
}
void AssetsTab::reassignCb(Fl_Widget*, void* v) {
    auto* t = static_cast<AssetsTab*>(v); long id = requireSelection(t);
    if (id >= 0) t->doReassign(id);
}
void AssetsTab::deleteCb(Fl_Widget*, void* v) {
    auto* t = static_cast<AssetsTab*>(v); long id = requireSelection(t);
    if (id >= 0) t->doDelete(id);
}
void AssetsTab::prevCb(Fl_Widget*, void* v) {
    auto* t = static_cast<AssetsTab*>(v);
    if (t->page_ > 0) { --t->page_; t->reload(); }
}
void AssetsTab::nextCb(Fl_Widget*, void* v) {
    auto* t = static_cast<AssetsTab*>(v);
    if (t->hasNext_) { ++t->page_; t->reload(); }
}
