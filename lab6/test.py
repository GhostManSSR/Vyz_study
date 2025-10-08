from locust import HttpUser, task, between
import base64

class OpenBMCUser(HttpUser):
    wait_time = between(1, 3)
    host = "https://localhost:2443"

    def on_start(self):
        credentials = base64.b64encode(b"root:0penBmc").decode("utf-8")
        self.headers = {
            "Authorization": f"Basic {credentials}",
            "Content-Type": "application/json"
        }
    
    @task
    def test_get_system_info(self):
        self.client.get("/redfish/v1/System/system", headers=self.headers, verify=False)

    @task
    def get_power_state(self):
        self.client.get("/redfish/v1/System/system", headers=self.headers, verify=False)

class PublicAPIUser(HttpUser):
    wait_time = between(1, 3)
    host = "https://jsonplaceholder.typicode.com"

    @task
    def get_posts(self):
        self.client.get("/posts")
    
    @task
    def get_weather(self):
        self.client.get("https://wttr.in/Novosibirsk?format=j1")