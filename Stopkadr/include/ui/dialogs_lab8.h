#pragma once
#include "service.h"
#include <FL/Fl_Window.H>
#include <FL/Fl_Choice.H>
#include <FL/Fl_Input.H>
#include <FL/Fl_Check_Button.H>
#include <FL/Fl_Multi_Browser.H>
#include <FL/Fl_Box.H>
#include <functional>
#include <string>
#include <vector>
#include <utility>

// Additional FLTK dialogs for the 8th laboratory assignment.
class SearchDialog : public Fl_Window {
public:
    using OnApply = std::function<void(const std::string&, bool, const std::string&)>;
    explicit SearchDialog(OnApply cb);
private:
    OnApply onApply_;
    Fl_Choice* category_;
    Fl_Check_Button* free_;
    Fl_Input* model_;
    static void applyCb(Fl_Widget*, void*);
};

class HistoryDialog : public Fl_Window {
public:
    HistoryDialog(const std::string& inv, const std::vector<HistoryEntry>& entries);
};

class BulkIssueDialog : public Fl_Window {
public:
    using OnIssue = std::function<bool(const std::vector<long>&, long)>;
    BulkIssueDialog(OnIssue cb,
                    const std::vector<std::pair<long, std::string>>& employees,
                    const std::vector<std::pair<long, std::string>>& assets);
private:
    OnIssue onIssue_;
    std::vector<std::pair<long, std::string>> employees_, assets_;
    Fl_Choice* employee_;
    Fl_Multi_Browser* assetsBrowser_;
    Fl_Box* countLabel_;
    static void issueCb(Fl_Widget*, void*);
    static void selectionCb(Fl_Widget*, void*);
    void updateCount();
};

class ReassignDialog : public Fl_Window {
public:
    using OnReassign = std::function<bool(long)>;
    ReassignDialog(OnReassign cb, const std::string& currentHolder,
                   const std::vector<std::pair<long, std::string>>& employees);
private:
    OnReassign onReassign_;
    std::vector<std::pair<long, std::string>> employees_;
    Fl_Choice* employee_;
    static void reassignCb(Fl_Widget*, void*);
};
