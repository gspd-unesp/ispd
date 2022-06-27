package ispd.DescreveSistema;
import java.awt.*;
import java.util.HashSet;
import java.util.Vector;
import java.util.List;
import java.util.ArrayList;
import javax.swing.*;
import java.io.*;

public class DescreveSistema implements Serializable{

	private List<DescreveIcone> listaIcones;
	private HashSet<String> listaNos;
	private int numArestas;
	private int numVertices;
	private int numIcones;
	private Boolean cargasConfiguradas;
	private Integer cargasTipoConfiguracao;
	private String cargasConfiguracao;


	public DescreveSistema(){
		listaIcones = new ArrayList<DescreveIcone>();
		listaNos = new HashSet<String>();
	}

	public void addIconeLista(int tipoIcone,int IdLocal,int IdGlobal,String nome,int x,int y,int prex,int prey,double poderComputacional,double taxaOcupacao,double latencia,double banda,boolean mestre,String algoritmoEscalonamento,List<Integer> escravos,HashSet<Integer> conexaoEntrada,HashSet<Integer> conexaoSaida, int noOrigem, int noDestino, int numeroEscravos){
		
		DescreveIcone dados = new DescreveIcone(tipoIcone,IdLocal,IdGlobal,nome,x,y,prex,prey,poderComputacional,taxaOcupacao,latencia,banda,mestre,algoritmoEscalonamento, escravos, conexaoEntrada, conexaoSaida,noOrigem,noDestino, numeroEscravos);

		listaIcones.add(dados);

	}

	public void setCargas(Boolean cargasConfiguradas, Integer cargasTipoConfiguracao, String cargasConfiguracao){
		this.cargasConfiguradas = cargasConfiguradas;
		this.cargasTipoConfiguracao = cargasTipoConfiguracao;
		this.cargasConfiguracao = cargasConfiguracao;
	}

	public void addListaNosLista(HashSet<String> lista){
		this.listaNos = lista;
	}
	
	public void addIDs(int numIcones, int numVertice, int numAresta){
		this.numIcones = numIcones;
		this.numArestas = numAresta;
		this.numVertices = numVertice;
	}

	public HashSet<String> getListaNosLista(){
		return listaNos;
	}

	public List<DescreveIcone> getIconeLista(){
		return listaIcones;
	}

	public int getNumVertices(){
		return numVertices;
	}
	
	public int getNumArestas(){
		return numArestas;
	}

	public int getNumIcones(){
		return numIcones;
	}

	public Boolean getCargasConfiguradas(){
		return cargasConfiguradas;
	}

	public Integer getCargasTipoConfiguracao(){
		return cargasTipoConfiguracao;
	}

	public String getCargasConfiguracao(){
		return cargasConfiguracao;
	}

}
