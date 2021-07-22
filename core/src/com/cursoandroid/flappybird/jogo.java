package com.cursoandroid.flappybird;

import com.badlogic.gdx.ApplicationAdapter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Random;

public class jogo extends ApplicationAdapter {

	//TEXTURAS
	private SpriteBatch batch;
	private Texture[] passaros;
	private Texture fundo;
	private Texture canoBaixo;
	private Texture canoTopo;
	private Texture gameOver;

	//Formas para colisão:
    private ShapeRenderer shapeRenderer;
    private Circle circuloPassaro;
    private Rectangle rectanguloCanoCima;
    private Rectangle rectanguloCanoBaixo;

	//ATRIBUTOS DE CONFIGURAÇÕES
	private float larguraDispositivo;
	private float alturaDispositivo;
	private float variacao = 0;
	private float gravidade = 0;
	private float posicaoInicialVerticalPassaro = 0;
	private float posicaoCanoHorizontal;
	private float posicaoCanoVertical;
	private float espacoEntreCanos;
	private Random random;
	private int pontos = 0;
	private int pontuacaoMaxima = 0;
	private boolean passouCano = false;
	private int estadoJogo = 0;
	private float posicaoHorizontalPassaro = 0;

	//Exibição de Texto:
    BitmapFont textPontuacao;
    BitmapFont textoReiniciar;
    BitmapFont textoMelhorPontuacao;

    //Objetos para a camera :
    private OrthographicCamera camera; //manipula a camera
    private Viewport viewport; // view
    private final float VIRTUAL_WIDTH = 720; //largura da view
    private final float VIRTUAL_HEIGHT = 1280; //altura da view;

	//MEU CODIGO
	private float larguraDoPassaro;
	private float alturaDoPassaro;

	//CONFIGURAÇÃO DOS SONS:
    Sound somVoando;
    Sound somColisao;
    Sound somPontuacao;

    //OBJETO SALVAR PONTUAÇÃO:
    Preferences preferencias;



	@Override
	public void create () {
        inicializarObjetos();
        inicializarTexturas();

	}

	@Override
	public void render () {
	    //LIMPAR OS FRAMES ANTERIOES:
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        verificarEstadoJogo();
        validarPontos();
        desenharTexturas();
        detectarColisoes();
	}
	private void verificarEstadoJogo(){
        //aplicando o eveto de click na tela:
        boolean toqueTela = Gdx.input.justTouched();

        if(estadoJogo == 0){ //passaro parado
            if(toqueTela){// Verifica se a tela for clicada!
                gravidade  = -14;
                estadoJogo = 1;
                somVoando.play();
            }
        }else if(estadoJogo == 1){ //jogo iniciado
            if(toqueTela){
                gravidade  = -14;
                somVoando.play();
            }
            //MOVIMENTAR O CANO:
            posicaoCanoHorizontal -= Gdx.graphics.getDeltaTime() * 620;


            if(posicaoCanoHorizontal < -canoTopo.getWidth()){
                posicaoCanoHorizontal = larguraDispositivo;
                posicaoCanoVertical = random.nextInt(800) - 400;
                passouCano = false;
            }


            //APLICANDO A GRAVIDADE DO PASSARO:
            if(posicaoInicialVerticalPassaro > 0 || toqueTela)
                posicaoInicialVerticalPassaro = posicaoInicialVerticalPassaro - gravidade;
            gravidade++;

        }else if( estadoJogo == 2){ //colidiu

            if(pontos > pontuacaoMaxima){
                pontuacaoMaxima = pontos;
                preferencias.putInteger("pontuacaoMaxima", pontuacaoMaxima);

                preferencias.flush();
            }

            posicaoHorizontalPassaro -= Gdx.graphics.getDeltaTime()* 900;
            if(toqueTela){// Verifica se a tela for clicada!
                estadoJogo = 0;//volta ao primeiro estado
                pontos = 0; //zera os pontos
                gravidade = 0; //zera a gravidade
                posicaoInicialVerticalPassaro = alturaDispositivo/2; //volta a posição do passaro inicial
                posicaoCanoHorizontal = larguraDispositivo; // volta a posião inicial do cano
                posicaoHorizontalPassaro=0;
                passouCano = false;
            }
        }

    }
    private void detectarColisoes(){
        circuloPassaro.set(50 + posicaoHorizontalPassaro + larguraDoPassaro/2,alturaDoPassaro/2 +posicaoInicialVerticalPassaro,passaros[0].getWidth()/2);
        rectanguloCanoBaixo.set(
                posicaoCanoHorizontal,alturaDispositivo / 2 - canoBaixo.getHeight() - espacoEntreCanos/2 +
                        posicaoCanoVertical,canoBaixo.getWidth() ,canoBaixo.getHeight()
        );
        rectanguloCanoCima.set(
                posicaoCanoHorizontal,alturaDispositivo / 2 + espacoEntreCanos/2 +
                        posicaoCanoVertical,canoTopo.getWidth() ,canoTopo.getHeight()
        );
        boolean colidiuCanoCima = Intersector.overlaps(circuloPassaro, rectanguloCanoCima);
        boolean colidiuCanoBaixo = Intersector.overlaps(circuloPassaro, rectanguloCanoBaixo);

        if(colidiuCanoBaixo || colidiuCanoCima){
            Gdx.app.log("Log","Colidiu");
            if(estadoJogo == 1){
                estadoJogo = 2;
                somColisao.play();
            }

        }

        /*shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.RED);

        shapeRenderer.circle(50 + larguraDoPassaro/2,alturaDoPassaro/2 +posicaoInicialVerticalPassaro,passaros[0].getWidth()/2);
        //Topo
        shapeRenderer.rect(
                posicaoCanoHorizontal,alturaDispositivo / 2 + espacoEntreCanos/2 +
                        posicaoCanoVertical,canoTopo.getWidth() + 20,canoTopo.getHeight()+700);

        shapeRenderer.rect(
                posicaoCanoHorizontal,alturaDispositivo / 2 - canoBaixo.getHeight() - espacoEntreCanos/2 +
                        posicaoCanoVertical,canoBaixo.getWidth() +20,canoBaixo.getHeight()+700);

        shapeRenderer.end();*/
    }

	private void desenharTexturas(){

	    batch.setProjectionMatrix(camera.combined); //configura a camera
        batch.begin(); // mostra que estamos começando a renderizar a imagem

        batch.draw(fundo,0,0, larguraDispositivo, alturaDispositivo);
        batch.draw(passaros[(int) variacao],50 + posicaoHorizontalPassaro,posicaoInicialVerticalPassaro,larguraDoPassaro, alturaDoPassaro); //desenha uma imagem ou uma textura dentro do jogo
        //posicaoCanoHorizontal -= 10;
        batch.draw(canoBaixo,posicaoCanoHorizontal,alturaDispositivo / 2 - canoBaixo.getHeight() - espacoEntreCanos/2+ posicaoCanoVertical);
        batch.draw(canoTopo,posicaoCanoHorizontal,alturaDispositivo / 2 + espacoEntreCanos/2 + posicaoCanoVertical);
        textPontuacao.draw(batch, String.valueOf(pontos), larguraDispositivo/2, alturaDispositivo-110);

        if( estadoJogo == 2 ){
            batch.draw(gameOver,larguraDispositivo/2 - gameOver.getWidth()/4,alturaDispositivo/2);
            textoReiniciar.draw(batch,"Toque na tela para reiniciar!",larguraDispositivo/2 -100,alturaDispositivo/2 - gameOver.getHeight()/2);
            textoMelhorPontuacao.draw(batch,"Seu record é: "+pontuacaoMaxima+" pontos",larguraDispositivo/2 -70,alturaDispositivo/2 - gameOver.getHeight());
        }

        batch.end(); //final do meu desenho
    }

    private void validarPontos(){
	    if(posicaoCanoHorizontal < 50){
	        if(!passouCano){
	            pontos++;
	            passouCano = true;
	            somPontuacao.play();
            }
        }

        variacao+= Gdx.graphics.getDeltaTime() * 10;
        if(variacao > 3){
            variacao = 0;
        }
    }

	private void inicializarTexturas(){
	    passaros = new Texture[3];
        passaros[0] = new Texture("passaro1.png");
        passaros[1] = new Texture("passaro2.png");
        passaros[2] = new Texture("passaro3.png");

        fundo = new Texture("fundo.png");
        canoBaixo = new Texture("cano_baixo_maior.png");
        canoTopo = new Texture("cano_topo_maior.png");
        gameOver = new Texture("game_over.png");
    }
    private void inicializarObjetos(){
        batch = new SpriteBatch();
        random= new Random();

        //MEDIDAS:
        larguraDispositivo = VIRTUAL_WIDTH;
        alturaDispositivo = VIRTUAL_HEIGHT;
        larguraDoPassaro = larguraDispositivo/10;
        alturaDoPassaro = alturaDispositivo/24;
        posicaoInicialVerticalPassaro = alturaDispositivo/2;
        posicaoCanoHorizontal = larguraDispositivo;
        espacoEntreCanos = 270; // 940

        //CONFIGURAÇÕES DOS TEXTOS:
        textPontuacao = new BitmapFont();    // instanciando a classe
        textPontuacao.setColor(Color.WHITE); //cor do texto
        textPontuacao.getData().setScale(6); //tamanho da fonte

        textoReiniciar = new BitmapFont();
        textoReiniciar.setColor(Color.GREEN);
        textoReiniciar.getData().setScale(2);

        textoMelhorPontuacao = new BitmapFont();
        textoMelhorPontuacao.setColor(Color.RED);
        textoMelhorPontuacao.getData().setScale(2);

        //Formas Geometricas para colisoes:
        shapeRenderer = new ShapeRenderer();
        circuloPassaro = new Circle();
        rectanguloCanoBaixo = new Rectangle();
        rectanguloCanoCima = new Rectangle();

        //Inicialzia sons:
         somVoando = Gdx.audio.newSound(Gdx.files.internal("som_asa.wav"));
         somColisao = Gdx.audio.newSound(Gdx.files.internal("som_batida.wav"));
         somPontuacao = Gdx.audio.newSound(Gdx.files.internal("som_pontos.wav"));

         //Configura preferencias dos objetos:
        preferencias = Gdx.app.getPreferences("flappyBird");
        pontuacaoMaxima = preferencias.getInteger("pontuacaoMaxima",0);

        //Configuraçao da camera:
        camera = new OrthographicCamera();
        camera.position.set(VIRTUAL_WIDTH/2,VIRTUAL_HEIGHT/2,0);
        viewport = new StretchViewport(VIRTUAL_WIDTH,VIRTUAL_HEIGHT,camera);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
	public void dispose () {

	}
}
