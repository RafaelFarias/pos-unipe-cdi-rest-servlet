package pos.unipe.example.cdi.rest.servlet;

import java.io.Serializable;

public class Usuario implements Serializable {

	private static final long serialVersionUID = 1759519498924913993L;

	private int id;
	
	private String nome;
	
	private String cpf;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getCpf() {
		return cpf;
	}

	public void setCpf(String cpf) {
		this.cpf = cpf;
	}

	@Override
	public String toString() {
		return "Usuario [id=" + id + ", nome=" + nome + ", cpf=" + cpf + "]";
	}
	
}
