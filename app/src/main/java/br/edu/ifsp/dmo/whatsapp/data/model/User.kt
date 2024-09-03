package br.edu.ifsp.dmo.whatsapp.data.model

class User() {
    var nome: String? = null
    var email: String? = null

    constructor(nome: String, email: String) : this() {
        this.nome = nome
        this.email = email
    }
}
