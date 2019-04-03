//TODO: Implement
class LoginService {
    constructor() {
        if(!LoginService.instance) {
            LoginService.instance = this;
            this.loggedin = true;
        }
    }

    isLoggedIn() {
        return this.loggedin;
    }

    getPicture() {
        return "https://www.gravatar.com/avatar/393db4e5e8992396545bd0ce9fce39ab?s=20&d=musikbot.elite12.de/img/favicon_small.png";
    }

    getName() {
        return "sven";
    }

    isAdmin() {
        return true;
    }

    login(username, password) {
        return new Promise((resolve,reject) => {
            setTimeout(() => {
                this.loggedin = true;
                resolve(true);
            }, 2000);
        });
    }

    logout() {
        this.loggedin = false;
    }
}
const instance = new LoginService();

export default instance;