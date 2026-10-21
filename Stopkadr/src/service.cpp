#include "service.h"
#include "employee-odb.hxx"
#include "asset-odb.hxx"
#include "assignment-odb.hxx"
#include <odb/transaction.hxx>
#include <odb/query.hxx>
#include <ctime>

using QEmp = odb::query<Employee>;
using QAst = odb::query<Asset>;
using QAsg = odb::query<Assignment>;

// ==================== Фотографы ====================

// ГОТОВО (образец): список всех фотографов.
std::vector<Employee> Service::employees() {
    odb::transaction t(db_.begin());
    std::vector<Employee> out;
    odb::result<Employee> r = db_.query<Employee>();
    for (auto it = r.begin(); it != r.end(); ++it)
        out.push_back(*it);
    t.commit();
    return out;
}

// ГОТОВО (образец): добавление фотографа с проверкой уникальности email.
void Service::addEmployee(const std::string& name, const std::string& email) {
    odb::transaction t(db_.begin());
    if (!db_.query<Employee>(QEmp::email == email).empty())
        throw DomainError("Фотограф с email " + email + " уже существует");
    Employee e;
    e.setFullName(name);
    e.setEmail(email);
    e.setActive(true);
    db_.persist(e);
    t.commit();
}

// ЗАДАЧА: обновление данных фотографа.
//
// Вызывается из диалога редактирования после клика «Сохранить».
// Меняет ФИО, email и статус «в штате»/«уволен».
//
// Контракт:
//   - Вход: id — существующий фотограф. name, email — новые значения.
//           active — true (в штате) или false (уволен).
//   - Успех: поля в БД обновлены, ничего не возвращается (void).
//   - Ошибки — выбросить DomainError:
//       1) если email изменился и такой email уже есть у другого фотографа,
//          текст: "Фотограф с email <email> уже существует";
//       2) если active становится false, а за фотографом есть техника
//          на съёмке, текст: "У фотографа на руках техника — сначала
//          примите возврат".
//   - При любой из ошибок изменения в БД не применяются.
//
// Критерии готовности:
//   - Изменение ФИО/email/статуса без нарушения правил — сохраняется
//     и видно в таблице после перерисовки.
//   - Попытка сохранить дубль email — fl_alert с текстом выше,
//     диалог не закрывается.
//   - Попытка уволить с техникой на съёмке — fl_alert с текстом выше,
//     статус в таблице не меняется.
void Service::updateEmployee(long id, const std::string& name,
                              const std::string& email, bool active) {
    odb::transaction t(db_.begin());
    auto e = db_.load<Employee>(id);
    if (e->email() != email && !db_.query<Employee>(QEmp::email == email).empty())
        throw DomainError("Фотограф с email " + email + " уже существует");
    if (!active && !db_.query<Assignment>(
            QAsg::employeeId == id && QAsg::returnedAt.is_null()).empty())
        throw DomainError("У фотографа на руках техника — сначала примите возврат");
    e->setFullName(name);
    e->setEmail(email);
    e->setActive(active);
    db_.update(*e);
    t.commit();
}

// ==================== Техника ====================

// ЗАДАЧА: список всей техники с указанием текущего держателя.
//
// Вызывается при каждой перерисовке вкладки «Техника».
//
// Контракт:
//   - Вход: нет.
//   - Успех: возвращает std::vector<AssetRow>, по одному элементу
//           на каждую единицу техники в БД. В поле holder — ФИО фотографа,
//           если техника сейчас на съёмке, или пустая строка, если в стойке.
//   - Ошибок не бросает (пустая БД — пустой вектор).
//
// Критерии готовности:
//   - После выдачи техники её holder = ФИО фотографа.
//   - После возврата holder = "".
//   - Фильтр «В стойке» / «На съёмке» в UI работает корректно
//     (фильтрация делается на стороне UI по полю asset.isAssigned(),
//     но holder должен быть правильным для обеих категорий).
std::vector<AssetRow> Service::assets() {
    odb::transaction t(db_.begin());
    std::vector<AssetRow> out;
    auto result = db_.query<Asset>();
    for (const auto& asset : result) {
        AssetRow row{asset, ""};
        if (asset.isAssigned()) {
            auto assignments = db_.query<Assignment>(
                QAsg::assetId == asset.id() && QAsg::returnedAt.is_null());
            for (const auto& assignment : assignments) {
                auto employee = db_.load<Employee>(assignment.employeeId());
                row.holder = employee->fullName();
                break;
            }
        }
        out.push_back(std::move(row));
    }
    t.commit();
    return out;
}

// ЗАДАЧА: добавление единицы техники в каталог.
//
// Вызывается из диалога «+ Техника» после клика «Сохранить».
//
// Контракт:
//   - Вход: inv — инвентарный номер, model, category — текстовые поля.
//   - Успех: в БД появляется одна запись в assets со статусом
//           assigned = false. Ничего не возвращается (void).
//   - Ошибка — выбросить DomainError:
//       если такой inventoryNumber уже есть, текст:
//       "Инвентарный номер <inv> уже существует".
//   - При ошибке изменения в БД не применяются.
//
// Критерии готовности:
//   - После добавления — строка в таблице с бейджем «В стойке»,
//     запись есть в SELECT * FROM assets.
//   - Попытка добавить дубль инвентарного номера — fl_alert
//     с текстом выше, в БД остаётся одна запись.
void Service::addAsset(const std::string& inv, const std::string& model,
                       const std::string& cat) {
    odb::transaction t(db_.begin());
    if (!db_.query<Asset>(QAst::inventoryNumber == inv).empty())
        throw DomainError("Инвентарный номер " + inv + " уже существует");
    Asset a;
    a.setInventoryNumber(inv);
    a.setModel(model);
    a.setCategory(cat);
    a.setAssigned(false);
    db_.persist(a);
    t.commit();
}

// ==================== Выдача ====================

// ЗАДАЧА (главная в лабе): оформление выдачи техники фотографу.
//
// Вызывается из диалога «Выдать» после клика «Сохранить».
//
// Контракт:
//   - Вход: assetId, employeeId — id существующих записей.
//   - Успех: техника переходит в статус assigned = true,
//           в assignments появляется запись с issuedAt = текущее время
//           и пустым returnedAt. Ничего не возвращается (void).
//   - Ошибки — выбросить DomainError:
//       1) техника уже на съёмке:
//          "Техника уже выдана";
//       2) фотограф уволен:
//          "Нельзя выдать уволенному фотографу".
//   - При любой ошибке изменения в БД не применяются.
//
// Критерии готовности:
//   - После выдачи: бейдж «На съёмке», «У кого» = ФИО, запись в assignments.
//   - Попытка выдать уже выданную технику — fl_alert с текстом,
//     в БД ничего не меняется.
//   - Попытка выдать уволенному — fl_alert с текстом, в БД ничего не меняется.
//   - Если прервать процесс между обновлением техники и созданием записи
//     о выдаче (например, временно сломать БД) — статус техники
//     не должен остаться «на съёмке».
void Service::issue(long assetId, long employeeId) {
    odb::transaction t(db_.begin());
    auto asset = db_.load<Asset>(assetId);
    auto employee = db_.load<Employee>(employeeId);
    if (asset->isAssigned())
        throw DomainError("Техника уже выдана");
    if (!employee->isActive())
        throw DomainError("Нельзя выдать уволенному фотографу");
    asset->setAssigned(true);
    db_.update(*asset);
    Assignment assignment;
    assignment.setAssetId(assetId);
    assignment.setEmployeeId(employeeId);
    assignment.setIssuedAt(std::time(nullptr));
    db_.persist(assignment);
    t.commit();
}

// ЗАДАЧА: возврат техники со съёмки обратно в стойку.
//
// Вызывается из главного окна кнопкой «Принять».
//
// Контракт:
//   - Вход: assetId — id техники.
//   - Успех: у активной выдачи проставляется returnedAt = текущее время,
//           у техники assigned = false. Ничего не возвращается (void).
//   - Ошибка — выбросить DomainError:
//       если активной выдачи нет (техника уже в стойке):
//       "Нет активной выдачи для этой техники".
//   - При ошибке изменения в БД не применяются.
//
// Критерии готовности:
//   - После возврата: бейдж «В стойке», «У кого» = «—»,
//     в assignments у записи появилась дата возврата.
//   - Попытка вернуть технику из стойки — fl_alert с текстом,
//     в БД ничего не меняется.
//   - Возвращённую технику можно снова выдать.
void Service::giveBack(long assetId) {
    odb::transaction t(db_.begin());
    auto result = db_.query<Assignment>(
        QAsg::assetId == assetId && QAsg::returnedAt.is_null());
    auto it = result.begin();
    if (it == result.end())
        throw DomainError("Нет активной выдачи для этой техники");
    Assignment assignment = *it;
    assignment.setReturnedAt(std::time(nullptr));
    db_.update(assignment);
    auto asset = db_.load<Asset>(assetId);
    asset->setAssigned(false);
    db_.update(*asset);
    t.commit();
}

// ==================== Для диалогов ====================

// ГОТОВО (образец): список активных фотографов для выпадающего списка.
std::vector<Employee> Service::activeEmployees() {
    odb::transaction t(db_.begin());
    std::vector<Employee> out;
    odb::result<Employee> r = db_.query<Employee>(QEmp::active == true);
    for (auto it = r.begin(); it != r.end(); ++it)
        out.push_back(*it);
    t.commit();
    return out;
}

// ГОТОВО (образец): список свободной техники для выпадающего списка.
std::vector<Asset> Service::freeAssets() {
    odb::transaction t(db_.begin());
    std::vector<Asset> out;
    odb::result<Asset> r = db_.query<Asset>(QAst::assigned == false);
    for (auto it = r.begin(); it != r.end(); ++it)
        out.push_back(*it);
    t.commit();
    return out;
}