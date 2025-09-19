
export class HttpProvider {
    private linkApi = "http://localhost:8080";
    private headers = "token";
    private url: string;


    constructor(link:string) {
        this.url = link;
    }

    get_auth():void{
        fetch(`${this.linkApi}/${this.url}`).then(res => res.json()).then(data => console.log(data)).catch(null);
    }

    post_auth():void{
        fetch(`${this.linkApi}/${this.url}`).then(res => res.json()).then(data => console.log(data)).catch(null);
    }

    get():void{
        fetch(`${this.linkApi}/${this.url}`).then(res => res.json()).then(data => console.log(data)).catch(null);
    }

    post():void{
        fetch(`${this.linkApi}/${this.url}`).then(res => res.json()).then(data => console.log(data)).catch(null);
    }
}