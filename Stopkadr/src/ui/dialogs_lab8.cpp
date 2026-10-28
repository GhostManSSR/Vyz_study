#include "ui/dialogs_lab8.h"
#include "ui/theme.h"
#include <FL/Fl_Button.H>
#include <FL/Fl_Browser.H>
#include <FL/fl_ask.H>
#include <ctime>
#include <cstdio>
#include <utility>

namespace {
void makeCancel(Fl_Window* w, int x, int y) {
    auto* btn = Theme::secondary(x, y, 116, 36, "Отмена");
    btn->callback([](Fl_Widget*, void* data) {
        static_cast<Fl_Window*>(data)->hide();
    }, w);
}
std::string formatTime(std::time_t t) {
    if (!t) return "—";
    char buf[64];
    std::tm* tm = std::localtime(&t);
    if (!tm || !std::strftime(buf, sizeof(buf), "%d.%m.%Y %H:%M", tm))
        return "(неизвестно)";
    return buf;
}
} // namespace

SearchDialog::SearchDialog(OnApply cb)
    : Fl_Window(460, 340, "Поиск техники"), onApply_(std::move(cb)) {
    set_modal();
    color(Theme::bg);
    Theme::fieldLabel(24, 26, 408, "Категория");
    category_ = new Fl_Choice(24, 50, 412, 36);
    category_->add("Все|Камера|Объектив|Вспышка|Штатив|Свет|Другое");
    category_->value(0);
    Theme::fieldLabel(24, 112, 408, "Подстрока модели");
    model_ = Theme::input(24, 136, 412, 36);
    free_ = new Fl_Check_Button(24, 192, 412, 36, "Только свободные");
    makeCancel(this, 180, 270);
    auto* apply = Theme::primary(312, 270, 124, 36, "Поиск");
    apply->callback(applyCb, this);
    end();
}
void SearchDialog::applyCb(Fl_Widget*, void* data) {
    auto* d = static_cast<SearchDialog*>(data);
    const char* cat = d->category_->text(d->category_->value());
    d->onApply_(cat ? cat : "Все", d->free_->value() != 0,
                d->model_->value());
    d->hide();
}

HistoryDialog::HistoryDialog(const std::string& inv,
                             const std::vector<HistoryEntry>& entries)
    : Fl_Window(840, 510, "История выдач") {
    set_modal();
    color(Theme::bg);
    const std::string label = "История выдач: " + inv;
    auto* title = new Fl_Box(24, 14, 792, 34, "");
    title->copy_label(label.c_str());
    title->labelsize(18);
    title->labelfont(Theme::fontBold);
    auto* browser = new Fl_Browser(24, 62, 792, 388);
    browser->textsize(13);
    browser->add("ФОТОГРАФ                        ВЫДАНО                      ВОЗВРАТ                       СТАТУС");
    for (const auto& entry : entries) {
        std::string who = entry.employeeName;
        if (who.size() > 26) who = who.substr(0, 26);
        char row[1000];
        const std::string issued = formatTime(entry.assignment.issuedAt());
        const std::string returned = formatTime(entry.assignment.returnedAt());
        std::snprintf(row, sizeof(row), "%-32s %-27s %-27s %s",
                      who.c_str(), issued.c_str(), returned.c_str(),
                      entry.assignment.isActive() ? "На съёмке" : "Возвращена");
        browser->add(row);
    }
    if (entries.empty()) browser->add("История выдач пока пуста");
    auto* close = Theme::primary(690, 463, 126, 36, "Закрыть");
    close->callback([](Fl_Widget*, void* v) {
        static_cast<HistoryDialog*>(v)->hide();
    }, this);
    end();
}

BulkIssueDialog::BulkIssueDialog(OnIssue cb,
                      const std::vector<std::pair<long, std::string>>& employees,
                      const std::vector<std::pair<long, std::string>>& assets)
    : Fl_Window(590, 580, "Выдать несколько"), onIssue_(std::move(cb)),
      employees_(employees), assets_(assets) {
    set_modal();
    color(Theme::bg);
    Theme::fieldLabel(24, 20, 540, "Фотограф");
    employee_ = new Fl_Choice(24, 44, 542, 36);
    for (const auto& e : employees_) employee_->add(e.second.c_str());
    if (!employees_.empty()) employee_->value(0);
    Theme::fieldLabel(24, 100, 542, "Техника — удерживайте Ctrl для выбора нескольких строк");
    assetsBrowser_ = new Fl_Multi_Browser(24, 128, 542, 342);
    for (const auto& asset : assets_) assetsBrowser_->add(asset.second.c_str());
    assetsBrowser_->callback(selectionCb, this);
    countLabel_ = new Fl_Box(24, 477, 542, 24, "");
    countLabel_->align(FL_ALIGN_LEFT | FL_ALIGN_INSIDE);
    updateCount();
    makeCancel(this, 310, 526);
    auto* issue = Theme::primary(446, 526, 120, 36, "Выдать всё");
    issue->callback(issueCb, this);
    end();
}
void BulkIssueDialog::updateCount() {
    int n = 0;
    for (int i = 1; i <= assetsBrowser_->size(); ++i)
        if (assetsBrowser_->selected(i)) ++n;
    const std::string label = "Выбрано: " + std::to_string(n) +
                              " из " + std::to_string(assets_.size());
    countLabel_->copy_label(label.c_str());
}
void BulkIssueDialog::selectionCb(Fl_Widget*, void* v) {
    static_cast<BulkIssueDialog*>(v)->updateCount();
}
void BulkIssueDialog::issueCb(Fl_Widget*, void* v) {
    auto* d = static_cast<BulkIssueDialog*>(v);
    if (d->employees_.empty()) { fl_alert("Нет фотографов в штате"); return; }
    std::vector<long> ids;
    for (int i = 1; i <= d->assetsBrowser_->size(); ++i)
        if (d->assetsBrowser_->selected(i)) ids.push_back(d->assets_[i - 1].first);
    if (d->onIssue_(ids, d->employees_[d->employee_->value()].first))
        d->hide();
}

ReassignDialog::ReassignDialog(OnReassign cb,
                               const std::string& currentHolder,
                               const std::vector<std::pair<long, std::string>>& employees)
    : Fl_Window(470, 275, "Сменить фотографа"), onReassign_(std::move(cb)),
      employees_(employees) {
    set_modal();
    color(Theme::bg);
    Theme::fieldLabel(24, 26, 420, "Сейчас у фотографа");
    auto* who = new Fl_Box(24, 52, 420, 40, "");
    who->copy_label(currentHolder.c_str());
    who->align(FL_ALIGN_LEFT | FL_ALIGN_INSIDE);
    Theme::fieldLabel(24, 116, 420, "Передать фотографу");
    employee_ = new Fl_Choice(24, 140, 422, 40);
    for (const auto& e : employees_) employee_->add(e.second.c_str());
    if (!employees_.empty()) employee_->value(0);
    makeCancel(this, 210, 214);
    auto* change = Theme::primary(342, 214, 104, 36, "Сменить");
    change->callback(reassignCb, this);
    end();
}
void ReassignDialog::reassignCb(Fl_Widget*, void* v) {
    auto* d = static_cast<ReassignDialog*>(v);
    if (d->employees_.empty()) { fl_alert("Нет фотографов в штате"); return; }
    if (d->onReassign_(d->employees_[d->employee_->value()].first))
        d->hide();
}
