#pragma once
#include "domain.hxx"
#include <odb/pgsql/database.hxx>
#include <vector>
#include <string>
#include <cstddef>
#include <ctime>

struct AssetRow {
    Asset asset;
    std::string holder;
};

struct PagedAssets {
    std::vector<AssetRow> items;
    bool hasNext = false;
};

struct HistoryEntry {
    Assignment assignment;
    std::string employeeName;
};

class Service {
public:
    explicit Service(odb::pgsql::database& db) : db_(db) {}

    // ---------- Сотрудники ----------
    std::vector<Employee> employees();
    void addEmployee(const std::string& name, const std::string& email);
    void setEmployeeActive(long id, bool active);
    void updateEmployee(long id, const std::string& name,
                        const std::string& email, bool active);

    // ---------- Оборудование ----------
    std::vector<AssetRow> assets();
    void addAsset(const std::string& inv, const std::string& model,
                  const std::string& category);

    // ---------- Выдача ----------
    void issue(long assetId, long employeeId);
    void giveBack(long assetId);

    // ---------- Для диалогов ----------
    std::vector<Employee> activeEmployees();
    std::vector<Asset>    freeAssets();

    // ---------- Лабораторная 8 ----------
    void duplicateAsset(long sourceId, const std::string& newInv);
    void issueMultiple(const std::vector<long>& assetIds, long employeeId);
    std::vector<Asset> searchAssets(const std::string& category,
                                    bool onlyFree,
                                    const std::string& text);
    PagedAssets listAssetsPaged(int page, int pageSize,
                                const std::string& statusFilter = "");
    std::vector<HistoryEntry> assetHistory(long assetId);
    std::vector<Asset> employeeAssets(long employeeId);
    void reassignAsset(long assetId, long newEmployeeId);
    void deleteAssetCascade(long assetId);

private:
    odb::pgsql::database& db_;
};