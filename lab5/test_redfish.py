import pytest
import requests
import urllib3
import logging

urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler('redfish_tests.log'),
        logging.StreamHandler()
    ]
)
logger = logging.getLogger(__name__)

class RedfishClient:
    
    def __init__(self, base_url: str, username: str, password: str):
        self.base_url = base_url
        self.username = username
        self.password = password
        self.session = requests.Session()
        self.session.auth = (username, password)
        self.session.verify = False
        
    def request(self, method: str, endpoint: str, **kwargs) -> requests.Response:
        url = f"{self.base_url}{endpoint}"
        try:
            print(f"Выполнение {method} запроса: {url}")
            response = self.session.request(method, url, **kwargs)
            print(f"Ответ: {response.status_code}")
            return response
        except requests.exceptions.RequestException as e:
            print(f"Ошибка запроса: {e}")
            raise

@pytest.fixture(scope="session")
def redfish_client():
    print("Инициализация Redfish клиента")
    client = RedfishClient(
        base_url="https://localhost:2443/redfish/v1",
        username="root",
        password="0penBmc"
    )
    print("Redfish клиент готов")
    return client

@pytest.fixture(scope="function")
def auth_token(redfish_client):
    print("Получение токена аутентификации")
    response = redfish_client.request(
        "POST", 
        "/SessionService/Sessions",
        json={"UserName": redfish_client.username, "Password": redfish_client.password}
    )
    
    assert response.status_code == 201, f"Ошибка аутентификации: {response.status_code}"
    token = response.headers.get("X-Auth-Token")
    assert token, "Токен аутентификации не получен"
    
    print(f"Токен получен: {token[:20]}...")
    yield token
    
    session_id = response.json().get("Id")
    if session_id:
        redfish_client.request("DELETE", f"/SessionService/Sessions/{session_id}")
        print("Сессия очищена")

class TestRedfishAuthentication:
    
    def test_session_creation(self, redfish_client):
        print("ТЕСТ: Создание сессии Redfish")
        
        response = redfish_client.request(
            "POST", 
            "/SessionService/Sessions",
            json={"UserName": redfish_client.username, "Password": redfish_client.password}
        )
        
        assert response.status_code == 201, f"Ожидался статус 201, получен {response.status_code}"
        
        response_data = response.json()
        assert "Id" in response_data, "ID сессии не найден"
        assert "X-Auth-Token" in response.headers, "Токен аутентификации не найден"
        
        print("Сессия создана успешно")
        print(f"ID сессии: {response_data['Id']}")
        print(f"Токен: {response.headers['X-Auth-Token'][:30]}...")
        
        session_id = response_data["Id"]
        redfish_client.request("DELETE", f"/SessionService/Sessions/{session_id}")
        print("Тестовые данные очищены")

class TestSystemInfo:
    
    def test_get_system_info(self, redfish_client):
        print("ТЕСТ: Информация о системе")
        
        response = redfish_client.request("GET", "/Systems/system")
        
        assert response.status_code == 200, f"Ожидался статус 200, получен {response.status_code}"
        
        system_info = response.json()
        required_fields = ["Id", "Name", "PowerState", "Status"]
        
        for field in required_fields:
            assert field in system_info, f"Поле {field} не найдено в ответе"
        
        print("Информация о системе получена успешно")
        print(f"ID системы: {system_info['Id']}")
        print(f"Имя: {system_info['Name']}")
        print(f"Состояние питания: {system_info['PowerState']}")
        print(f"Статус: {system_info['Status']}")
        
        if 'Model' in system_info:
            print(f"Модель: {system_info['Model']}")
        if 'Manufacturer' in system_info:
            print(f"Производитель: {system_info['Manufacturer']}")

class TestPowerManagement:
    
    def test_power_control(self, redfish_client):
        print("ТЕСТ: Управление питанием")
        
        print("Получение текущего состояния системы")
        response = redfish_client.request("GET", "/Systems/system")
        current_state = response.json().get("PowerState", "Unknown")
        print(f"Текущее состояние питания: {current_state}")
        
        print("Отправка команды 'On'")
        response = redfish_client.request(
            "POST", 
            "/Systems/system/Actions/ComputerSystem.Reset",
            json={"ResetType": "On"}
        )
        
        assert response.status_code in [200, 202, 204], \
            f"Ожидался статус 200/202/204, получен {response.status_code}"
        
        print(f"Команда управления питанием выполнена. Статус: {response.status_code}")
        
        print("Проверка обновленного состояния")
        response = redfish_client.request("GET", "/Systems/system")
        new_state = response.json().get("PowerState", "Unknown")
        print(f"Новое состояние питания: {new_state}")

class TestThermalManagement:
    
    def test_thermal_sensors(self, redfish_client):
        print("ТЕСТ: Температурные датчики")
        
        print("Получение списка шасси")
        response = redfish_client.request("GET", "/Chassis")
        assert response.status_code == 200, "Не удалось получить список шасси"
        
        chassis_data = response.json()
        
        if "Members" not in chassis_data or not chassis_data["Members"]:
            print("Шасси не найдены")
            return
        
        print(f"Найдено шасси: {len(chassis_data['Members'])}")
        
        chassis_url = chassis_data["Members"][0]["@odata.id"]
        print(f"Получение информации о шасси: {chassis_url}")
        response = redfish_client.request("GET", chassis_url)
        chassis_info = response.json()
        
        if "Thermal" not in chassis_info:
            print("Раздел Thermal не найден в шасси")
            return
        
        print("Раздел Thermal найден")
        
        thermal_url = chassis_info["Thermal"]["@odata.id"]
        print(f"Получение температурных данных: {thermal_url}")
        response = redfish_client.request("GET", thermal_url)
        
        if response.status_code != 200:
            print(f"Thermal endpoint недоступен: {response.status_code}")
            return
        
        thermal_data = response.json()
        
        if "Temperatures" in thermal_data and thermal_data["Temperatures"]:
            print(f"Найдено датчиков температуры: {len(thermal_data['Temperatures'])}")
            for sensor in thermal_data["Temperatures"]:
                sensor_name = sensor.get("Name", "Unknown")
                temperature = sensor.get("ReadingCelsius", "N/A")
                units = sensor.get("ReadingUnits", "Celsius")
                print(f"Датчик {sensor_name}: {temperature} {units}")
        else:
            print("Температурные датчики не найдены")

class TestCPUSensors:
    
    def test_cpu_sensors_consistency(self, redfish_client):
        print("ТЕСТ: Датчики CPU")
        
        print("Получение информации о системе")
        response = redfish_client.request("GET", "/Systems/system")
        system_info = response.json()
        
        if "Processors" in system_info:
            processors_url = system_info["Processors"]["@odata.id"]
            print(f"Получение информации о процессорах: {processors_url}")
            response = redfish_client.request("GET", processors_url)
            processors_info = response.json()
            
            if "Members" in processors_info and processors_info["Members"]:
                print(f"Найдено процессоров: {len(processors_info['Members'])}")
                for i, processor in enumerate(processors_info["Members"]):
                    proc_url = processor["@odata.id"]
                    print(f"Получение данных процессора {i+1}: {proc_url}")
                    response = redfish_client.request("GET", proc_url)
                    proc_info = response.json()
                    print(f"Процессор {i+1}: {proc_info.get('Model', 'Unknown')}")
                    print(f"Производитель: {proc_info.get('Manufacturer', 'Unknown')}")
            else:
                print("Процессоры не найдены")
        else:
            print("Раздел Processors не найден")
        
        print("Проверка температурных датчиков")
        thermal_test = TestThermalManagement()
        thermal_test.test_thermal_sensors(redfish_client)

@pytest.fixture(scope="function", autouse=True)
def test_separator():
    print("НАЧАЛО ТЕСТА")
    yield
    print("ТЕСТ ЗАВЕРШЕН")

if __name__ == "__main__":
    print("Запуск тестов Redfish API")
    print("Для подробного вывода используйте: pytest test_redfish.py -v -s")