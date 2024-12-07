package Models;

public class Request {
    private int Id;
    private String pedido;
    private boolean aceite;
    private boolean rejeitado;
    private String coordenador;
    private String criador;

    public Request(int Id, String pedido, boolean aceite, boolean rejeitado, String coordenador, String criador) {
        this.Id = Id;
        this.pedido = pedido;
        this.aceite = aceite;
        this.rejeitado = rejeitado;
        this.coordenador = coordenador;
        this.criador = criador;
    }

    public int getId() {
        return Id;
    }

    public void setId(int Id) {
        this.Id = Id;
    }

    public String getPedido() {
        return pedido;
    }

    public void setPedido(String pedido) {
        this.pedido = pedido;
    }

    public boolean isAceite() {
        return aceite;
    }

    public void setAceite(boolean aceite) {
        this.aceite = aceite;
    }

    public boolean isRejeitado() {
        return rejeitado;
    }

    public void setRejeitado(boolean rejeitado) {
        this.rejeitado = rejeitado;
    }

    public String getCoordenador() {
        return coordenador;
    }

    public void setCoordenador(String coordenador) {
        this.coordenador = coordenador;
    }

    public String getCriador() {
        return criador;
    }

    public void setCriador(String criador) {
        this.criador = criador;
    }

}
