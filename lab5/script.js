

async function getDataFromAPI(params) {
    const responce = await fetch("https://dummyjson.com/products");

    console.log(responce.json())
}

async function calcStatsFromAPI() {
    const res = await loadData();
    let result = {};
    res.forEach(element => {
        const country = element.country || "Unknown";
        if (res[country]) {
            res[country]++;
        } else {
            res[country] = 1;
        }
    });
    return result;
}
