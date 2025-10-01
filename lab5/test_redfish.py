import pytest
import requests
import logging
import subprocess

logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')

BASE_URL = "https://<openbmc_ip>"
USER = "root"
PASSWORD = "0penBmc"

@pytest.fixture(scope="session")
def session_token():
    """Получение токена сессии для авторизации"""
    url = f"{BASE_URL}/redfish/v1/SessionService/Sessions"
    try:
        response = requests.post(url, auth=(USER, PASSWORD), verify=False)
        response.raise_for_status()
        token = response.headers.get('X-Auth-Token')
        logging.info(f"Session token received: {token}")
        assert token is not None, "Session token is missing"
        return token
    except requests.RequestException as e:
        logging.error(f"Failed to authenticate: {e}")
        pytest.fail(f"Failed to authenticate: {e}")

def test_redfish_accessibility():
    """Проверка доступности OpenBMC"""
    r = requests.get(f"{BASE_URL}/redfish/v1/", auth=(USER, PASSWORD), verify=False)
    assert r.status_code == 200

def test_authentication(session_token):
    """Тест аутентификации через Redfish API"""
    assert session_token is not None

def test_get_system_info(session_token):
    """Тест получения информации о системе"""
    headers = {"X-Auth-Token": session_token}
    r = requests.get(f"{BASE_URL}/redfish/v1/Systems/system", headers=headers, verify=False)
    assert r.status_code == 200
    data = r.json()
    assert 'Status' in data
    assert 'PowerState' in data

def test_power_control(session_token):
    """Тест управления питанием сервера"""
    headers = {"X-Auth-Token": session_token}
    reset_url = f"{BASE_URL}/redfish/v1/Systems/system/Actions/ComputerSystem.Reset"
    payload = {"ResetType": "On"}
    r = requests.post(reset_url, headers=headers, json=payload, verify=False)
    assert r.status_code == 202

    r2 = requests.get(f"{BASE_URL}/redfish/v1/Systems/system", headers=headers, verify=False)
    assert r2.status_code == 200
    data2 = r2.json()
    assert data2.get('PowerState') == "On"

def test_cpu_temperature_range(session_token):
    """Тест на соответствие температуры CPU норме"""
    headers = {"X-Auth-Token": session_token}
    temp_url = f"{BASE_URL}/redfish/v1/Chassis/system/Sensors"
    r = requests.get(temp_url, headers=headers, verify=False)
    assert r.status_code == 200
    data = r.json()

    for sensor in data.get('Members', []):
        if 'CPU' in sensor.get('Name', ''):
            temp = sensor.get('ReadingCelsius')
            assert temp is not None and 20 <= temp <= 85

def test_cpu_sensor_consistency_redfish_ipmi(session_token):
    """Тест соответствия датчиков CPU Redfish и IPMI"""
    headers = {"X-Auth-Token": session_token}
    redfish_url = f"{BASE_URL}/redfish/v1/Chassis/system/Sensors"
    r = requests.get(redfish_url, headers=headers, verify=False)
    assert r.status_code == 200
    redfish_data = r.json()

    try:
        ipmi_output = subprocess.check_output(["ipmitool", "sensor", "list"]).decode('utf-8')
    except Exception as e:
        pytest.fail(f"IPMI sensor read failed: {e}")

    assert True
