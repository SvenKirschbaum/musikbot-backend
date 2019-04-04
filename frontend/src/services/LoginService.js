class LoginService {
    constructor() {
        if(!LoginService.instance) {
            LoginService.instance = this;
            let state = JSON.parse(localStorage.getItem('loginstate'));
            if(state) {
                this.loggedin = state.loggedin;
                this.user = state.user;
                this.token = state.token;
            }
            else {
                this.loggedin = false;
                this.user = null;
                this.token = null;
            }
        }
    }

    isLoggedIn() {
        return this.loggedin;
    }

    getGravatarID() {
        if(this.user) return this.user.gravatarId;
    }

    getName() {
        if(this.user) return this.user.name;
    }

    isAdmin() {
        if(this.user) return this.user.admin;
    }

    getToken() {
        if(this.loggedin) return this.token;
    }

    login(username, password) {
        return new Promise((resolve,reject) => {
            fetch("/api/login", {
                method: 'POST',
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify({
                    'username': username,
                    'password': password
                })
            })
            .then(res => res.json())
            .then(response => {
                if(response.success) {
                    this.user = response.user;
                    this.loggedin = true;
                    localStorage.setItem('loginstate', JSON.stringify({
                        loggedin: true,
                        token: response.token,
                        user: response.user
                    }));
                    resolve();
                }
                else {
                    reject(response.error);
                }
            });
        });
    }

    logout() {
        this.loggedin = false;
        this.user = null;
        localStorage.removeItem('loginstate');
    }
}
const instance = new LoginService();

export default instance;