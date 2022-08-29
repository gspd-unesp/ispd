package ispd.arquivo.description;
import java.util.HashSet;
import java.util.List;
import java.io.*;


public class DescreveIcone implements Serializable{
	
		private int tipoIcone;
		private int IdGlobal;
		private int IdLocal;
		private String nome;
		private int x,y,prex,prey;
		private double poderComputacional;
		private double taxaOcupacao;
		private double latencia;
		private double banda;
		private int numeroEscravos;
		private boolean mestre;
		private List<Integer> escravos;
		private String algoritmoEscalonamento;
		private HashSet<Integer> conexaoEntrada;
		private HashSet<Integer> conexaoSaida;
		private int noOrigem;
		private int noDestino;

	
		public DescreveIcone(int tipoIcone,int IdLocal,int IdGlobal,String nome,int x,int y,int prex,int prey,double poderComputacional,double taxaOcupacao,double latencia,double banda,boolean mestre,String algoritmoEscalonamento,List<Integer> escravos,HashSet<Integer> conexaoEntrada,HashSet<Integer> conexaoSaida, int noOrigem, int noDestino, int numeroEscravos){
	
		this.tipoIcone = tipoIcone;
		this.IdLocal = IdLocal;
		this.IdGlobal = IdGlobal;
		this.nome = nome;
		this.x = x;
		this.y = y;
		this.prex = prex;
		this.prey = prey;
		this.poderComputacional = poderComputacional;
		this.taxaOcupacao = taxaOcupacao;
		this.latencia = latencia;
		this.banda = banda;
		this.mestre = mestre;
		this.escravos = escravos;
		this.algoritmoEscalonamento = algoritmoEscalonamento;
		this.conexaoSaida = conexaoSaida;
		this.conexaoEntrada = conexaoEntrada;
		this.numeroEscravos = numeroEscravos;
		this.noOrigem = noOrigem;
		this.noDestino = noDestino;
	
		}

		public int getTipoIcone(){return  tipoIcone;}
		public int getIdGlobal(){return  IdGlobal;}
		public int getIdLocal(){return  IdLocal;}
		public String getNome(){return  nome;}
		public int getX(){return  x;}
		public int getY(){return  y;}
		public int getPreX(){return  prex;}
		public int getPreY(){return  prey;}
		public double getPoderComputacional(){return  poderComputacional;}
		public double getTaxaOcupacao(){return  taxaOcupacao;}
		public double getLatencia(){return  latencia;}
		public double getBanda(){return  banda;}
		public boolean getMestre(){return  mestre;}
		public List<Integer> getEscravos(){return  escravos;}
		public String getAlgoritmoEscalonamento(){return  algoritmoEscalonamento;}
		public HashSet<Integer> getConexaoEntrada(){return  conexaoEntrada;}
		public HashSet<Integer> getConexaoSaida(){return  conexaoSaida;}
		public int getNumeroEscravos(){return numeroEscravos;}
		public int getNoOrigem(){return noOrigem;}
		public int getNoDestino(){return noDestino;}

}