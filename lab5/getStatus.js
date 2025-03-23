const getDataFromAPI = require("./getDataFromAPI")

async function getStatus() {
    const res = await getDataFromAPI.getDataFromAPI();
    let result = {};
    res.forEach(element => {
        const category = element.category || "Unknown";
        if (result[category]) {
            result[category]++;
        } else {
            result[category] = 1;
        }
    });
    return result;
}

module.exports.getStatus = getStatus