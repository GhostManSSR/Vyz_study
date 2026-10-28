#include "service.h"
#include "asset-odb.hxx"
#include "assignment-odb.hxx"
#include "employee-odb.hxx"
#include <odb/query.hxx>
#include <odb/transaction.hxx>
#include <algorithm>
#include <ctime>
#include <unordered_set>
#include <utility>

using QAst8 = odb::query<Asset>;
using QAsg8 = odb::query<Assignment>;

namespace {
// Called only while the caller's ODB transaction is active.
AssetRow makeAssetRow(odb::pgsql::database& db, const Asset& asset) {
    AssetRow row{asset, ""};
    if (asset.isAssigned()) {
        auto active = db.query<Assignment>(
            QAsg8::assetId == asset.id() && QAsg8::returnedAt.is_null());
        auto it = active.begin();
        if (it != active.end()) {
            auto photographer = db.load<Employee>(it->employeeId());
            row.holder = photographer->fullName();
        }
    }
    return row;
}
} // namespace

// Four reference operations supplied with the lab 8 patch.
std::vector<Asset> Service::searchAssets(const std::string& category,
                                        bool onlyFree,
                                        const std::string& text) {
    odb::transaction t(db_.begin());
    QAst8 q = QAst8::true_expr;
    if (!category.empty() && category != "Все")
        q = q && (QAst8::category == category);
    if (onlyFree)
        q = q && (QAst8::assigned == false);
    if (!text.empty()) {
        std::string pattern = "%" + text + "%";
        q = q && (QAst8::model.like(pattern) || QAst8::inventoryNumber.like(pattern));
    }
    std::vector<Asset> out;
    auto rows = db_.query<Asset>(q);
    for (const auto& a : rows) out.push_back(a);
    std::sort(out.begin(), out.end(), [](const Asset& a, const Asset& b) {
        return a.id() > b.id();
    });
    t.commit();
    return out;
}

PagedAssets Service::listAssetsPaged(int page, int pageSize,
                                    const std::string& statusFilter) {
    odb::transaction t(db_.begin());
    PagedAssets paged;
    QAst8 q = QAst8::true_expr;
    if (statusFilter == "В стойке")
        q = q && (QAst8::assigned == false);
    else if (statusFilter == "На съёмке")
        q = q && (QAst8::assigned == true);
    std::vector<Asset> all;
    auto rows = db_.query<Asset>(q);
    for (const auto& a : rows) all.push_back(a);
    std::sort(all.begin(), all.end(), [](const Asset& a, const Asset& b) {
        return a.id() > b.id();
    });
    if (page < 0) page = 0;
    if (pageSize <= 0) pageSize = 20;
    const std::size_t offset = static_cast<std::size_t>(page) * pageSize;
    const std::size_t end = offset > all.size() ? all.size() :
                            std::min(all.size(), offset + static_cast<std::size_t>(pageSize));
    for (std::size_t i = std::min(offset, all.size()); i < end; ++i)
        paged.items.push_back(makeAssetRow(db_, all[i]));
    paged.hasNext = offset + static_cast<std::size_t>(pageSize) < all.size();
    t.commit();
    return paged;
}

std::vector<HistoryEntry> Service::assetHistory(long assetId) {
    odb::transaction t(db_.begin());
    std::vector<HistoryEntry> out;
    auto rows = db_.query<Assignment>(QAsg8::assetId == assetId);
    for (const auto& a : rows) {
        auto employee = db_.load<Employee>(a.employeeId());
        out.push_back({a, employee->fullName()});
    }
    std::sort(out.begin(), out.end(), [](const HistoryEntry& a, const HistoryEntry& b) {
        if (a.assignment.issuedAt() != b.assignment.issuedAt())
            return a.assignment.issuedAt() > b.assignment.issuedAt();
        return a.assignment.id() > b.assignment.id();
    });
    t.commit();
    return out;
}

std::vector<Asset> Service::employeeAssets(long employeeId) {
    odb::transaction t(db_.begin());
    std::vector<Asset> out;
    auto rows = db_.query<Assignment>(
        QAsg8::employeeId == employeeId && QAsg8::returnedAt.is_null());
    for (const auto& a : rows)
        out.push_back(*db_.load<Asset>(a.assetId()));
    std::sort(out.begin(), out.end(), [](const Asset& a, const Asset& b) {
        return a.id() > b.id();
    });
    t.commit();
    return out;
}

// Part 5 (TODO): duplicate the catalogue entry, never its lending history.
void Service::duplicateAsset(long sourceId, const std::string& newInv) {
    odb::transaction t(db_.begin());
    auto source = db_.load<Asset>(sourceId);
    if (!db_.query<Asset>(QAst8::inventoryNumber == newInv).empty())
        throw DomainError("Инвентарный номер " + newInv + " уже существует");

    Asset copy;
    copy.setInventoryNumber(newInv);
    copy.setModel(source->model());
    copy.setCategory(source->category());
    copy.setAssigned(false); // Even when the original is currently on a shoot.
    db_.persist(copy);
    t.commit();
}

// Part 6 (TODO): all-or-nothing bulk issue.
void Service::issueMultiple(const std::vector<long>& assetIds, long employeeId) {
    odb::transaction t(db_.begin());
    if (assetIds.empty())
        throw DomainError("Не выбрано ни одной единицы техники");

    auto employee = db_.load<Employee>(employeeId);
    if (!employee->isActive())
        throw DomainError("Нельзя выдать уволенному фотографу");

    std::vector<long> checkedIds;
    std::unordered_set<long> seen;
    checkedIds.reserve(assetIds.size());
    for (long id : assetIds) {
        auto asset = db_.load<Asset>(id);
        if (asset->isAssigned() || !seen.insert(id).second)
            throw DomainError("Техника " + asset->inventoryNumber() + " уже выдана");
        checkedIds.push_back(id);
    }

    const std::time_t now = std::time(nullptr);
    for (long id : checkedIds) {
        auto asset = db_.load<Asset>(id);
        asset->setAssigned(true);
        db_.update(*asset);
        Assignment asg;
        asg.setAssetId(asset->id());
        asg.setEmployeeId(employeeId);
        asg.setIssuedAt(now);
        db_.persist(asg);
    }
    t.commit();
}

// Part 7 (TODO): close the old assignment and start a new one atomically.
void Service::reassignAsset(long assetId, long newEmployeeId) {
    odb::transaction t(db_.begin());
    auto asset = db_.load<Asset>(assetId);
    if (!asset->isAssigned())
        throw DomainError("Техника не выдана — используйте «Выдать»");

    auto newEmployee = db_.load<Employee>(newEmployeeId);
    if (!newEmployee->isActive())
        throw DomainError("Нельзя переназначить уволенному фотографу");

    auto result = db_.query<Assignment>(
        QAsg8::assetId == assetId && QAsg8::returnedAt.is_null());
    auto it = result.begin();
    if (it == result.end())
        throw DomainError("Нет активной выдачи");

    Assignment old = *it;
    if (old.employeeId() == newEmployeeId)
        throw DomainError("Техника уже у этого фотографа");

    const std::time_t now = std::time(nullptr);
    old.setReturnedAt(now);
    db_.update(old);

    Assignment next;
    next.setAssetId(assetId);
    next.setEmployeeId(newEmployeeId);
    next.setIssuedAt(now);
    db_.persist(next);
    // asset.assigned remains true throughout the transfer.
    t.commit();
}

// Part 8 (TODO): delete all linked issue history before the asset.
void Service::deleteAssetCascade(long assetId) {
    odb::transaction t(db_.begin());
    auto asset = db_.load<Asset>(assetId);
    if (asset->isAssigned())
        throw DomainError("Нельзя удалить выданную технику — сначала примите возврат");

    std::vector<long> assignmentIds;
    auto result = db_.query<Assignment>(QAsg8::assetId == assetId);
    for (const auto& asg : result)
        assignmentIds.push_back(asg.id());
    for (long id : assignmentIds)
        db_.erase<Assignment>(id);

    db_.erase<Asset>(assetId);
    t.commit();
}
