package pos.unipe.example.cdi.rest.servlet;

import java.io.Serializable;

public class Reserva implements Serializable {
	
	private static final long serialVersionUID = 1387338212260978184L;

	private int id;
	
	private Usuario usuario;

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	@Override
	public String toString() {
		return "Reserva [id=" + id + ", usuario=" + usuario + "]";
	}
}
