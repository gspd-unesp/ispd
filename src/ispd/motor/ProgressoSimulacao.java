/* ==========================================================
 * iSPD : iconic Simulator of Parallel and Distributed System
 * ==========================================================
 *
 * (C) Copyright 2010-2014, by Grupo de pesquisas em Sistemas Paralelos e Distribuídos da Unesp (GSPD).
 *
 * Project Info:  http://gspd.dcce.ibilce.unesp.br/
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * [Oracle and Java are registered trademarks of Oracle and/or its affiliates. 
 * Other names may be trademarks of their respective owners.]
 *
 * ---------------
 * ProgressoSimulacao.java
 * ---------------
 * (C) Copyright 2014, by Grupo de pesquisas em Sistemas Paralelos e Distribuídos da Unesp (GSPD).
 *
 * Original Author:  Denison Menezes (for GSPD);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 
 * 09-Set-2014 : Version 2.0;
 * 18-Set-2014 : Retirado analise de modelo iconico e simulável
 * 14-Out-2014 : Adicionado chamadas para realizar a simulação
 *
 */
package ispd.motor;

import ispd.arquivo.xml.IconicoXML;
import ispd.arquivo.xml.ManipuladorXML;
import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.Tarefa;
import ispd.motor.metricas.Metricas;
import java.awt.Color;
import java.io.File;
import java.util.List;
import org.w3c.dom.Document;

/**
 * Classe de conexão entre interface de usuario e motor de simulação
 *
 * @author denison
 */
public abstract class ProgressoSimulacao {

    public void println(String text, Color cor) {
        this.print(text, cor);
        this.print("\n", cor);
    }

    public void println(String text) {
        this.print(text, Color.black);
        this.print("\n", Color.black);
    }

    public void print(String text) {
        this.print(text, Color.black);
    }

    public abstract void incProgresso(int n);

    public abstract void print(String text, Color cor);

    public void validarInicioSimulacao(Document modelo) throws IllegalArgumentException {
        this.print("Verifying configuration of the icons.");
        this.print(" -> ");
        if (modelo == null) {
            this.println("Error!", Color.red);
            throw new IllegalArgumentException("The model has no icons.");
        }
        try {
            IconicoXML.validarModelo(modelo);
        } catch (IllegalArgumentException e) {
            this.println("Error!", Color.red);
            throw e;
        }
        this.incProgresso(5);
        this.println("OK", Color.green);
    }

    public Metricas simulacaoSequencial(Document doc) {
        //criar grade
        this.print("Mounting network queue.");
        this.print(" -> ");
        RedeDeFilas redeDeFilas = IconicoXML.newRedeDeFilas(doc);
        incProgresso(5);//[5%] --> 5%
        this.println("OK", Color.green);
        //criar tarefas
        this.print("Creating tasks.");
        this.print(" -> ");
        List<Tarefa> tarefas = IconicoXML.newGerarCarga(doc).toTarefaList(redeDeFilas);
        incProgresso(10);//[10%] --> 15%
        this.println("OK", Color.green);
        //Verifica recursos do modelo e define roteamento
        Simulacao sim = new SimulacaoSequencial(this, redeDeFilas, tarefas);
        incProgresso(5);//[5%] --> 20 %
        this.print("Creating routing.");
        this.print(" -> ");
        sim.criarRoteamento();
        incProgresso(15);//[15%] --> 35 %
        this.println("OK", Color.green);
        //Realiza asimulação
        this.println("Simulating.");
        //recebe instante de tempo em milissegundos ao iniciar a simulação
        double t1 = System.currentTimeMillis();
        sim.simular();
        incProgresso(30);//[30%] --> 65%
        this.println("OK", Color.green);
        //Recebe instnte de tempo em milissegundos ao fim da execução da simulação
        double t2 = System.currentTimeMillis();
        //Calcula tempo de simulação em segundos
        double tempototal = (t2 - t1) / 1000;
        this.println("Simulation Execution Time = " + tempototal + "seconds");
        //Obter Resultados
        this.print("Getting Results.");
        this.print(" -> ");
        Metricas metrica = sim.getMetricas();
        incProgresso(10);//[10%] --> 75%
        this.println("OK", Color.green);
        metrica.setRedeDeFilas(redeDeFilas);
        metrica.setTarefas(tarefas);
        return metrica;
    }

    public Metricas simulacaoSequencial(Document doc, int simNumbers) {
        Metricas metricas = new Metricas(IconicoXML.newListUsers(doc));
        RedeDeFilas redeDeFilas = null;
        List<Tarefa> tarefas = null;
        if (simNumbers <= 0) {
            throw new ExceptionInInitializerError("Number of simulations must be positive!");
        }
        int progresso = 70 / simNumbers;
        double t1 = System.currentTimeMillis();
        for (int i = 0; i < simNumbers; i++) {
            this.print("\n");
            this.print("Prepare Simulation");
            this.print(" " + i + " -> ");

            //recebe instante de tempo em milissegundos ao iniciar a simulação
            double start = System.currentTimeMillis();
            double temp1 = System.currentTimeMillis();
            redeDeFilas = IconicoXML.newRedeDeFilas(doc);
            //criar tarefas
            tarefas = IconicoXML.newGerarCarga(doc).toTarefaList(redeDeFilas);
            //Verifica recursos do modelo e define roteamento
            Simulacao sim = new SimulacaoSequencial(this, redeDeFilas, tarefas);//[10%] --> 40 %
            //Define roteamento
            sim.criarRoteamento();
            //Recebe instante de tempo em milissegundos ao fim da execução da simulação
            double t2 = System.currentTimeMillis();
            this.print("OK", Color.green);
            this.println(" time " + (t2 - temp1) + " ms");

            //Realiza asimulação
            this.print("Simulating");
            this.print(" -> ");
            temp1 = System.currentTimeMillis();
            sim.simular();//[30%] --> 70%
            t2 = System.currentTimeMillis();
            this.print("OK", Color.green);
            this.println(" time " + (t2 - temp1) + " ms");

            this.print("Getting Results.");
            this.print(" -> ");
            //Obter Resultados
            temp1 = System.currentTimeMillis();
            Metricas metrica = sim.getMetricas();
            metricas.addMetrica(metrica);
            t2 = System.currentTimeMillis();
            incProgresso(progresso);
            this.print("OK", Color.green);
            this.println(" time " + (t2 - temp1) + " ms");
            this.println("Execution Time = " + (t2 - start) + "ms");
        }
        //Calcula tempo de simulação em segundos
        double tempototal = (System.currentTimeMillis() - t1) / 1000;
        this.print("\n");
        this.println("Total Execution Time = " + tempototal + "seconds");
        if (simNumbers > 1) {
            metricas.calculaMedia();
        }
        incProgresso(75-(progresso*simNumbers));
        metricas.setRedeDeFilas(redeDeFilas);
        metricas.setTarefas(tarefas);
        return metricas;
    }

    public Metricas simulacaoOtimista(Document doc, int threads, int simNumbers) {
        Metricas metricas = new Metricas(IconicoXML.newListUsers(doc));
        RedeDeFilas redeDeFilas = null;
        List<Tarefa> tarefas = null;
        if (simNumbers <= 0) {
            throw new ExceptionInInitializerError("Number of simulations must be positive!");
        }
        if (threads <= 0) {
            throw new ExceptionInInitializerError("Number of threads must be positive!");
        }
        int progresso = 70 / simNumbers;
        double t1 = System.currentTimeMillis();
        for (int i = 1; i <= simNumbers; i++) {
            this.print("\n");
            this.print("Prepare Simulation");
            this.print(" " + i + " -> ");

            //recebe instante de tempo em milissegundos ao iniciar a simulação
            double start = System.currentTimeMillis();
            double temp1 = System.currentTimeMillis();
            //criar grade
            redeDeFilas = IconicoXML.newRedeDeFilas(doc);
            tarefas = IconicoXML.newGerarCarga(doc).toTarefaList(redeDeFilas);
            //Verifica recursos do modelo e define roteamento
            Simulacao sim = new SimulacaoParalela(this, redeDeFilas, tarefas, threads);
            //Define roteamento
            sim.criarRoteamento();
            //Recebe instante de tempo em milissegundos ao fim da execução da simulação
            double t2 = System.currentTimeMillis();
            this.print("OK", Color.green);
            this.println(" time " + (t2 - temp1) + " ms");

            //Realiza asimulação
            this.print("Simulating");
            this.print(" -> ");
            temp1 = System.currentTimeMillis();
            sim.simular();
            t2 = System.currentTimeMillis();
            this.print("OK", Color.green);
            this.println(" time " + (t2 - temp1) + " ms");

            this.print("Getting Results.");
            this.print(" -> ");
            //Obter Resultados
            Metricas temp = sim.getMetricas();
            metricas.addMetrica(temp);
            t2 = System.currentTimeMillis();
            incProgresso(progresso);
            this.print("OK", Color.green);
            this.println(" time " + (t2 - temp1) + " ms");
            this.println("Execution Time = " + (t2 - start) + "ms");
        }
        //Calcula tempo de simulação em segundos
        double tempototal = (System.currentTimeMillis() - t1) / 1000;
        this.print("\n");
        this.println("Total Execution Time = " + tempototal + "seconds");
        if (simNumbers > 1) {
            metricas.calculaMedia();
        }
        incProgresso(75 - (progresso*simNumbers));
        metricas.setRedeDeFilas(redeDeFilas);
        metricas.setTarefas(tarefas);
        return metricas;
    }

    public Metricas simulacoesParalelas(File arquivo, int numThreads, int numExecucoes) throws Exception {
        Document modelo[] = ManipuladorXML.clone(arquivo, numThreads);
        //Verifica se foi construido modelo corretamente
        this.validarInicioSimulacao(modelo[0]);
        return simulacoesParalelas(modelo, numThreads, numExecucoes);
    }

    public Metricas simulacoesParalelas(Document documento, int numThreads, int numExecucoes) throws Exception {
        Document modelo[] = ManipuladorXML.clone(documento, numThreads);
        return simulacoesParalelas(modelo, numThreads, numExecucoes);
    }

    private Metricas simulacoesParalelas(Document modelo[], int numThreads, int numExecucoes) throws Exception {
        if (numExecucoes <= 0) {
            throw new ExceptionInInitializerError("Number of simulations must be positive!");
        }
        if (numThreads <= 0) {
            throw new ExceptionInInitializerError("Number of threads must be positive!");
        }
        if (numThreads > numExecucoes) {
            numThreads = numExecucoes;
        }
        Metricas metricas = new Metricas(IconicoXML.newListUsers(modelo[0]));
        int progresso = 70 / numThreads;
        int inicio = 0, incremento = numExecucoes / numThreads;
        RunnableImpl[] trabalhador = new RunnableImpl[numThreads];
        Thread[] thread = new Thread[numThreads];
        this.println("Will run " + numThreads + " threads");
        double t1 = System.currentTimeMillis();
        for (int i = 0; i < numThreads - 1; i++) {
            trabalhador[i] = new RunnableImpl(modelo[i], incremento);
            thread[i] = new Thread(trabalhador[i]);
            thread[i].start();
            inicio += incremento;
        }
        trabalhador[numThreads - 1] = new RunnableImpl(modelo[numThreads - 1], numExecucoes - inicio);
        thread[numThreads - 1] = new Thread(trabalhador[numThreads - 1]);
        thread[numThreads - 1].start();
        for (int i = 0; i < numThreads; i++) {
            thread[i].join();
            incProgresso(progresso);
        }
        double t2 = System.currentTimeMillis();
        //Calcula tempo de simulação em segundos
        double tempototal = (t2 - t1) / 1000;
        this.println("Total Execution Time = " + tempototal + " seconds");
        this.print("Getting Results.");
        this.print(" -> ");
        if (numExecucoes > 1) {
            for (int i = 0; i < numThreads; i++) {
                metricas.addMetrica(trabalhador[i].getMetricas());
            }
            metricas.calculaMedia();
        }
        this.println("OK", Color.GREEN);
        incProgresso(75 - (progresso*numThreads));
        metricas.setRedeDeFilas(trabalhador[numThreads - 1].metricas.getRedeDeFilas());
        metricas.setTarefas(trabalhador[numThreads - 1].metricas.getTarefas());
        return metricas;
    }

    /**
     * Classe interna para executar uma simulação em thread
     */
    private class RunnableImpl implements Runnable {

        private final Document modelo;
        private final int numExecucaoThread;
        private Metricas metricas;
        private final ProgressoSimulacao progSim = new ProgressoSimulacao() {
            @Override
            public void incProgresso(int n) {
            }

            @Override
            public void print(String text, Color cor) {
            }
        };

        public RunnableImpl(Document modelo, int numExecucao) {
            this.modelo = modelo;
            this.numExecucaoThread = numExecucao;
            this.metricas = new Metricas(null);
        }

        public Metricas getMetricas() {
            return metricas;
        }

        @Override
        public void run() {
            RedeDeFilas redeDeFilas = null;
            List<Tarefa> tarefas = null;
            for (int i = 0; i < numExecucaoThread; i++) {
                //criar grade
                redeDeFilas = IconicoXML.newRedeDeFilas(modelo);
                tarefas = IconicoXML.newGerarCarga(modelo).toTarefaList(redeDeFilas);
                //Verifica recursos do modelo e define roteamento
                Simulacao sim = new SimulacaoSequencial(progSim, redeDeFilas, tarefas);
                //Define roteamento
                sim.criarRoteamento();
                //Realiza asimulação
                sim.simular();
                Metricas temp = sim.getMetricas();
                metricas.addMetrica(temp);
            }
            metricas.setRedeDeFilas(redeDeFilas);
            metricas.setTarefas(tarefas);
        }
    }
}
