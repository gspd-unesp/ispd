package ispd.Interface;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;
import java.lang.*;
import java.util.HashSet;
import java.util.Vector;
import java.util.List;
import java.util.ArrayList; 
import java.awt.geom.AffineTransform;
import java.io.*;

public class Icone implements Serializable
  {
  private int x,y,prex,prey;
  private int tipoIcone;
  private int idLocal;
  private int idGlobal;
  private boolean estaAtivo;
  private boolean configurado;
  private ImageIcon iconeMaquina;
  private ImageIcon iconeConfigurado;
  private transient Image imageMaquina;
  private transient Image imageConfigurado;
  private String nome;
  private double poderComputacional;
  private double taxaOcupacao;
  private double latencia;
  private double banda;
  private boolean mestre;
  private int numeroEscravos;
  private HashSet<Integer> conexaoEntrada;
  private HashSet<Integer> conexaoSaida;
  private HashSet<Integer> nosIndiretosEntrada;
  private HashSet<Integer> nosIndiretosSaida;
  private HashSet<Integer> nosEscalonaveis;
  private List<Integer> escravos;
  private String algoritmoEscalonamento;
  private int noOrigem;
  private int noDestino;
  private Polygon areaSeta;
  private Rectangle areaRect;

public Icone(int x,int y,int prex,int prey,int tipoIcone,int idLocal, int idGlobal){
	this.x = x;
	this.y = y;
	this.prex = prex;
	this.prey = prey;
	this.idLocal = idLocal;
	this.idGlobal = idGlobal;
	estaAtivo = false;
	configurado = false;
	noOrigem = 0;
	noDestino = 0;
	this.tipoIcone=tipoIcone;
	conexaoEntrada = new HashSet<Integer>();
	conexaoSaida = new HashSet<Integer>();
	nosIndiretosEntrada = new HashSet<Integer>();
	nosIndiretosSaida = new HashSet<Integer>();
	nosEscalonaveis = new HashSet<Integer>();
	if(this.tipoIcone==2) 
		areaSeta = new Polygon();
	else
		areaRect = new Rectangle(x-15,y-15,30,30);
	initParametros();
}

public Icone(int x,int y,int tipoIcone,int idLocal, int idGlobal){
	this.x = x;
	this.y = y; 
	this.prex = 0;
	this.prey = 0;
	this.idLocal = idLocal;
	this.idGlobal = idGlobal;
	estaAtivo = false;
	configurado = false;
	noOrigem = 0;
	noDestino = 0;
	this.tipoIcone=tipoIcone;
	conexaoEntrada = new HashSet<Integer>();
	conexaoSaida = new HashSet<Integer>();
	nosIndiretosEntrada = new HashSet<Integer>();
	nosIndiretosSaida = new HashSet<Integer>();
	nosEscalonaveis = new HashSet<Integer>();
	areaRect = new Rectangle(x-15,y-15,30,30);
	initParametros();
}


/**###########################################
#	Metodo para setar os valores padões  #
#quando um novo icone é criado.              #
############################################*/
	private void initParametros(){
		nome = "";
		poderComputacional = 0.0;
		taxaOcupacao = 0.0;
		latencia = 0.0;
		banda = 0.0;
		mestre = false;
		numeroEscravos = 0;
		escravos = new ArrayList<Integer>();
		algoritmoEscalonamento = "---";
		switch(this.tipoIcone){
			case 1: iconeMaquina = new ImageIcon(getClass().getResource("imagens/botao_no.gif")); break;
			case 3: iconeMaquina = new ImageIcon(getClass().getResource("imagens/botao_cluster.gif")); break;
			case 4: iconeMaquina = new ImageIcon(getClass().getResource("imagens/botao_internet.gif")); break;
		}
	}




/**#####################################################
#	Metodo responsavel por desenhar o icone na tela#
#ele recebe como parametro um elemento da classe       #
#Graphics2D                                            #
######################################################*/
	public void draw(Graphics2D g2d){
		
		this.verificaConfiguracao();
		
		if(configurado)
			iconeConfigurado = new ImageIcon(getClass().getResource("imagens/verde.gif"));
		else
			iconeConfigurado = new ImageIcon(getClass().getResource("imagens/vermelho.gif"));
		
		
		if(tipoIcone==2){
			int x1 = (int) (((((prex+x)/2)+x)/2)+x)/2;
			int y1 = (int) (((((prey+y)/2)+y)/2)+y)/2;

			if(configurado)	
				g2d.setColor(new Color(0,130,0));
			else
				g2d.setColor(new Color(255,0,0));
			//g2d.drawLine(x,y,prex,prey);
			
			drawArrow( g2d, prex, prey, x, y );
			//g2d.drawString(String.valueOf(idGlobal),x1,y1);
		}else{
			imageMaquina = iconeMaquina.getImage();
			imageConfigurado = iconeConfigurado.getImage();
			g2d.drawImage(imageMaquina,x-15,y-15,null);
			g2d.drawImage(imageConfigurado,x+15,y+15,null);
			g2d.setColor(new Color(0,0,0));
			g2d.drawString(String.valueOf(idGlobal),x,y+30);
		}
		
		
		// Se o icone estiver ativo, desenhamos uma margem nele.
		if (estaAtivo){
			if(tipoIcone!=2){
				g2d.setColor(new Color(255,0,0));
				g2d.drawRect(x-19,y-17,37,34);
			}else{
				g2d.setColor(new Color(0,0,0));
				drawArrow( g2d, prex, prey, x, y );

			}
		}
	}


/**#####################################################
#	Metodo responsavel por verificar se a posiçao      #
#em que o mouse está localizado, corresponde a posição #
#ao redor do icone, ou seja corresponde a area onde o  #
#icone está desenhado. O método recebe como parametro  #
#a posição x e y do ponteiro do mouse e retorna true se#
#a posição estiver na area do icone ou false caso con- #
#trario.                                               #
######################################################*/
	public boolean getRectEnvolvente(int tx, int ty){

		if(tipoIcone==2){
			return areaSeta.contains(tx,ty);
		}else{
			return areaRect.contains(tx,ty);
		}

	}

/**######################################################
#	Metodo responsavel por verificar se o icone está    #
#devidamente configurado, ele é chamado toda vez que o  #
#icone é desenhado na tela.                             #
#######################################################*/
	public void verificaConfiguracao(){
		this.setConfigurado(false);
		switch(tipoIcone){
			case 1:{
				if(nome.length()!=0 && poderComputacional!=0.0 && mestre==false){
					this.setConfigurado(true);
				}else if(nome.length()!=0 && poderComputacional!=0.0  && mestre==true && algoritmoEscalonamento!="---" && !escravos.isEmpty()){
						this.setConfigurado(true);
					}
				}
			case 2:{
				if(nome.length()!=0 && banda!=0.0  && latencia!=0.0)
					this.setConfigurado(true);
				}
			case 3:{
				if(nome.length()!=0 && banda!=0.0 && poderComputacional!=0.0 && latencia!=0.0 && numeroEscravos!=0 && algoritmoEscalonamento!="---")
					this.setConfigurado(true);
				}
			case 4:{
				if(nome.length()!=0 && banda!=0.0 && taxaOcupacao!=0.0 && latencia!=0.0)
					this.setConfigurado(true);
				}
		}
	}

/*#####################################################
#	Metodo responsavel por atualizar os icones    #
#####################################################*/

	public void move(){
		this.verificaConfiguracao();
	}

/*#############################################################
#	Metodos Add e Remove para as configurações do icone   #
#############################################################*/

	public void addIdConexaoEntrada(int id){
		conexaoEntrada.add(id);
	}

	public void addIdConexaoSaida(int id){
		conexaoSaida.add(id);
	}

	public boolean containsConexaoEntrada(int id){
		return conexaoEntrada.contains(id);
	}

	public boolean containsConexaoSaida(int id){
		return conexaoSaida.contains(id);
	}

	public void removeConexaoEntrada(int i){
		conexaoEntrada.remove(i);
	}

	public void removeConexaoSaida(int i){
		conexaoSaida.remove(i);
	}

	public void clearNosIndiretosEntrada(){
		nosIndiretosEntrada.clear();
	}

	public void clearNosIndiretosSaida(){
		nosIndiretosSaida.clear();
	}
	
	public void clearNosEscalonaveis(){
		nosEscalonaveis.clear();
	}
	
/*#####################################################
#	Metodos Sets para as configurações do icone   #
#####################################################*/

	public void setNoDestino(int No){
		this.noDestino = No;
	}

	public void setNoOrigem(int No){
		this.noOrigem = No;
	}

	public void setEstaAtivo(boolean estaAtivo){
		this.estaAtivo = estaAtivo;
	}

	public void setPosition(int x,int y){
		this.x = x;
		this.y = y;
		if(tipoIcone!=2)
			areaRect.setLocation(x-15,y-15);
	}
	
	public void setPrePosition(int prex,int prey){
		this.prex = prex;
		this.prey = prey;
	}

	public void setConfigurado(boolean set){
		this.configurado = set;
	}

	public void setConexaoEntrada(HashSet<Integer> conexaoEntrada){
		this.conexaoEntrada = conexaoEntrada;
	}

	public void setConexaoSaida(HashSet<Integer> conexaoSaida){
		this.conexaoSaida = conexaoSaida;
	}

	public void setObjetoNosIndiretosEntrada(HashSet<Integer> nosIndiretos){
		this.nosIndiretosEntrada = nosIndiretos;
	}

	public void setObjetoNosIndiretosSaida(HashSet<Integer> nosIndiretos){
		this.nosIndiretosSaida = nosIndiretos;
	}
	
	public void setObjetoNosEscalonaveis(HashSet<Integer> nosEscalonaveis){
		this.nosEscalonaveis = nosEscalonaveis;
	}

	public void setEscravos(List<Integer> temp){
		this.escravos = temp;
	}
	
	public void setNome(String nome){
		this.nome = nome;
	}
	
	public void setLatencia(double latencia){
		this.latencia = latencia;
	}
	
	public void setBanda(double banda){
		this.banda = banda;
	}
	
	public void setNumeroEscravos(int numeroEscravos){
		this.numeroEscravos = numeroEscravos;
	}
	
	public void setPoderComputacional(double poderComputacional){
		this.poderComputacional = poderComputacional;
	}
	
	public void setTaxaOcupacao(double taxaOcupacao){
		this.taxaOcupacao = taxaOcupacao;
	}
	
	public void setMestre(boolean mestre){
		this.mestre = mestre;
	}
	
	public void setAlgoritmo(String algoritmo){
		this.algoritmoEscalonamento = algoritmo;
	}

/*#####################################################
#	Metodos Gets para as configurações do icone   #
#####################################################*/

	public int getNoDestino(){
		return noDestino;
	}

	public int getNoOrigem(){
		return noOrigem;
	}

	public boolean getEstaAtivo(){
		return estaAtivo;
	}

	public Vector<Integer> getNosEscalonaveis(){
		Vector<Integer> vetor = new Vector<Integer>();
		vetor.addAll(nosEscalonaveis);
		return vetor;
	}
	
	public HashSet<Integer> getObjetoConexaoEntrada(){
		return conexaoEntrada;
	}

	public HashSet<Integer> getObjetoConexaoSaida(){
		return conexaoSaida;
	}

	public HashSet<Integer> getObjetoNosIndiretosEntrada(){
		return nosIndiretosEntrada;
	}

	public HashSet<Integer> getObjetoNosIndiretosSaida(){
		return nosIndiretosSaida;
	}

	public HashSet<Integer> getObjetoNosEscalonaveis(){
		return nosEscalonaveis;
	}
	
	public int getIdGlobal(){
		return idGlobal;
	}

	public int getIdLocal(){
		return idLocal;
	}
	
	public int getNumX(){
		return x;
	}
	
	public int getNumY(){
		return y;
	}
	
	public int getNumPreX(){
		return prex;
	}
	
	public int getNumPreY(){
		return prey;
	}
	
	public int getTipoIcone(){
		return tipoIcone;
	}
	
	public boolean getMestre(){
		return mestre;
	}
	
	public String getNome(){
		return nome;
	}
	
	public double getLatencia(){
		return latencia;
	}
	
	public double getBanda(){
		return banda;
	}
	
	public int getNumeroEscravos(){
		return numeroEscravos;
	}
	
	public double getPoderComputacional(){
		return poderComputacional;
	}
	
	public double getTaxaOcupacao(){
		return taxaOcupacao;
	}
	
	public boolean getConfigurado(){
		return configurado;
	}
	
	public String getAlgoritmo(){
		return algoritmoEscalonamento;
	}
	
	public List<Integer> getEscravos(){
		return escravos;
	}
	

  private void drawArrow( Graphics2D g, int xo, int yo, int xd, int yd )
  {

	double x1 = (((((xo+xd)/2)+xd)/2)+xd)/2;
	double y1 = (((((yo+yd)/2)+yd)/2)+yd)/2;

    double arrowWidth = 11.0f ;
    double theta = 0.423f ;
    int[] xPoints = new int[ 3 ] ;
    int[] yPoints = new int[ 3 ] ;
    double[] vecLine = new double[ 2 ] ;
    double[] vecLeft = new double[ 2 ] ;
    double fLength;
    double th;
    double ta;
    double baseX, baseY ;

    xPoints[ 0 ] = (int)x1 ;
    yPoints[ 0 ] = (int)y1 ;

    // build the line vector
    vecLine[ 0 ] = (double)xPoints[ 0 ] - xo ;
    vecLine[ 1 ] = (double)yPoints[ 0 ] - yo ;

    // build the arrow base vector - normal to the line
    vecLeft[ 0 ] = -vecLine[ 1 ] ;
    vecLeft[ 1 ] = vecLine[ 0 ] ;

    // setup length parameters
    fLength = (double)Math.sqrt( vecLine[0] * vecLine[0] + vecLine[1] * vecLine[1] ) ;
    th = arrowWidth / ( 2.0f * fLength ) ;
    ta = arrowWidth / ( 2.0f * ( (double)Math.tan( theta ) / 2.0f ) * fLength ) ;

    // find the base of the arrow
    baseX = ( (double)xPoints[ 0 ] - ta * vecLine[0]);
    baseY = ( (double)yPoints[ 0 ] - ta * vecLine[1]);

    // build the points on the sides of the arrow
    xPoints[ 1 ] = (int)( baseX + th * vecLeft[0] );
    yPoints[ 1 ] = (int)( baseY + th * vecLeft[1] );
    xPoints[ 2 ] = (int)( baseX - th * vecLeft[0] );
    yPoints[ 2 ] = (int)( baseY - th * vecLeft[1] );

    g.drawLine( xo, yo, xd, yd ) ;
    g.fillPolygon( xPoints, yPoints, 3 ) ;

    areaSeta.reset();
    areaSeta.addPoint(xPoints[0], yPoints[0]);
    areaSeta.addPoint(xPoints[1], yPoints[1]);
    areaSeta.addPoint(xPoints[2], yPoints[2]);
	
  }


}