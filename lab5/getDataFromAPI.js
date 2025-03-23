async function getDataFromAPI() {
    try{
        const response = await fetch("https://dummyjson.com/products", {
            method: "GET",
            headers: {
                "Content-Type": "application/json",
            },
        });
        const json = await response.json();
        return json.products
    }
    catch (error) {
        throw new Error("HTTP error link or network!");
    }
}

module.exports.getDataFromAPI = getDataFromAPI
