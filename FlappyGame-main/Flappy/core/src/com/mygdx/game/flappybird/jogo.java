package com.mygdx.game.flappybird;

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

    //criação
    private SpriteBatch batch;    //metodo interno que associa as informações que serão renderizadas em tela

    private Texture[] passaros;   //armazena imagens do passaro
    private Texture fundo;        //amazena as imagens do background
    private Texture canoTopo;     //armazena as imagem do cano de cima
    private Texture canoBaixo;    //armazena as imagem do cano de baixo
    private Texture gameOver;     //armazena as imagem do gameover
    private Texture flappyLogo;   //armazena as imagem do logo.
    private Texture ouroCoin;     //armazena as imagem da moeda de ouro.
    private Texture prataCoin;    //armazena as imagem da  moeda de prata.


    private int pontos = 0;            //pontuação
    private int pontuacaoMaxima = 0;   //highscore
    private int pontuacaoOuro = 10;    //valor da moeda de ouro
    private int pontuacaoPrata = 5;    //valor da moeda de prata
    private int estadoJogo = 0;        //controla os estados do game


    BitmapFont textoIniciar;            //texto iniciar
    BitmapFont textoPontuacao;          //texto pontuação
    BitmapFont textoReiniciar;          //texto reiniciar
    BitmapFont textoMelhorPontuacao;    //texto highscore

    Preferences preferencias;

    //movimentação do jogador
    private int gravidade = 0;                        //puxa o jogador "para baixo"

    private float variacao = 0;                       //varia as alturas
    private float posicaoInicialVerticalPassaro = 0;  //posição inicial do jogador
    private float posicaoCanoHorizontal = 0;          //posição horizontal do cano
    private float posicaoCanoVertical;                //posição vertical do cano
    private float espaçoEntreCanos;                   //distância entre os canos
    private float posicaoHorizontalPassaro;           //posição horizontal do jogador

    private float posicaoCoinHorizontal = 0;          //posição horizontal da moeda
    private float posicaoCoinVertical = 0;            //posição vertical da moeda

    private boolean passouCano = false;               //verifica passagem do jogador pelo cano

    private Random random;                            //torna os espaços entre os canos aleatorio

    //Tela
    private float larguraDispositivo;  //ajusta para a largura do celular
    private float alturaDispositivo;   //ajusta para a altura do celular

    //Colisão
    private ShapeRenderer shapeRenderer;        //renderiza os colisores do jogo
    private Circle circuloPassaro;              //collider do passaro
    private Rectangle retanguloCanoCima;        //collider do cano de cima
    private Rectangle retanguloCanoBaixo;       //collider do cano de baixo
    private Circle circuloCoinOuro;             //collider da moeda de ouro
    private Circle circuloCoinPrata;            //collider da moeda de prata

    //Sons
    Sound somVoando;      //som para o voo
    Sound somColisão;     //som para a colisão
    Sound somPontuacao;   //som para a pontuação
    Sound somCoin;        //som para pegar a moeda

    //camera
    private OrthographicCamera camera;                   //seta a camera
    private Viewport viewport;                           //pega a ViewPort
    private final float VIRTUAL_WIDTH = 720;             //largura da tela
    private final float VIRTUAL_HEIGHT = 1280;           //altura da tela


    @Override
    public void create() {


        inicializaTexturas();
        inicializaObjetos();

        //Create: Gera os objetos na tela
    }

    @Override
    public void render() {

        Gdx.gl.glClear((GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT));       //otimiza a renderização

        verificarEstadoJogo();
        validarPontos();
        desenharTexturas();
        detectarColisão();

        //render:gera o layout e as infomações do game (pontuação, etc.)
    }




    @Override
    public void dispose() {


    }

    private void inicializaTexturas() {

        passaros = new Texture[3];                             //gera as imagens do passado
        passaros[0] = new Texture("Pidgey_01.png");            //primeira imagem do pidgey
        passaros[1] = new Texture("Pidgey_02.png");            //segunda imagem do pidgey
        passaros[2] = new Texture("Pidgey_03.png");            //terceira imagem do pidgey

        fundo = new Texture("fundo.png");                      //imagem do background
        canoTopo = new Texture("cano_topo_maior.png");         //imagem do cano de cima
        canoBaixo = new Texture("cano_baixo_maior.png");       //imagem do cano de baixo

        ouroCoin = new Texture("PokeCoin_Gold.png");           //imagem moeda de ouro
        prataCoin = new Texture("PokeCoin_Silver.png");        //imagem moeda de prata

        flappyLogo = new Texture("Flappymon_logo.png");        //imagem do logo
        gameOver = new Texture("game_over.png");               //imagem do gameOver

    }

    private void inicializaObjetos() {

        batch = new SpriteBatch();                               //instancia objetos
        random = new Random();                                   //randomiza os canos

        larguraDispositivo = VIRTUAL_WIDTH;                     //pega a largura do dispositivo passada anteriormente
        alturaDispositivo = VIRTUAL_HEIGHT;                     //pega a altura do dispositivo passada anteriormente
        posicaoInicialVerticalPassaro = alturaDispositivo / 2;  //posiciona o jogador no centro (em relação a altura, na vertical)
        posicaoCanoHorizontal = larguraDispositivo;             //torna a posição do cano igual a largura do celular
        espaçoEntreCanos = 350;                                 //distancia entre os canos

        textoPontuacao = new BitmapFont();                      //torna o texto da pontuação um bitmapfont
        textoPontuacao.setColor(Color.WHITE);                   //escolhe a cor do texto (nesse caso o branco)
        textoPontuacao.getData().setScale(10);                  //tamanho do texto

        textoMelhorPontuacao = new BitmapFont();                //torna o texto do highscore um bitmapfont
        textoMelhorPontuacao.setColor(Color.RED);               //escolhe a cor do texto (nesse caso o vermelho)
        textoMelhorPontuacao.getData().setScale(2);             //tamanho do texto

        textoReiniciar = new BitmapFont();                      //torna o texto reiniciar um bitmapfont
        textoReiniciar.setColor(Color.GREEN);                   //escolhe a cor do texto (nesse caso o verde)
        textoReiniciar.getData().setScale(2);                   //tamanho do texto

        textoIniciar = new BitmapFont();                        //torna o texto iniciar um bitmapfont
        textoIniciar.setColor(Color.GREEN);                     //escolhe a cor do texto (nesse caso o verde)
        textoIniciar.getData().setScale(2);                     //tamanho do texto

        shapeRenderer = new ShapeRenderer();                    //inicia render dos colisores
        circuloPassaro = new Circle();                          //collider circulo para o jogador
        retanguloCanoCima = new Rectangle();                    //collider retangulo do cano de cima
        retanguloCanoBaixo = new Rectangle();                   //collider retangulo do cano de baixo
        circuloCoinOuro = new Circle();                         //collider circulo da moeda de ouro
        circuloCoinPrata = new Circle();                        //collider circulo da moeda de prata

        somColisão = Gdx.audio.newSound(Gdx.files.internal("som_batida.wav"));     //toca o  som de colisão
        somVoando = Gdx.audio.newSound(Gdx.files.internal("som_asa.wav"));         //toca o  som de voo
        somPontuacao = Gdx.audio.newSound(Gdx.files.internal("som_pontos.wav"));   //toca o  som da pontuação
        somCoin = Gdx.audio.newSound(Gdx.files.internal("som-de-moedas.wav"));     //toca o  som da coleta de moedas

        preferencias = Gdx.app.getPreferences("flappybird");                      //armazenar as preferências
        pontuacaoMaxima = preferencias.getInteger("pontuacaoMaxima", 0);          //pega o highscore armazenado

        camera = new OrthographicCamera();                                         //camera agora é ortográfica
        camera.position.set(VIRTUAL_WIDTH / 2, VIRTUAL_HEIGHT / 2, 0);             //gera a camera com as medidas
        viewport = new StretchViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);     //viewPort é um stretch que segue os parametros

    }

    private void verificarEstadoJogo() {

        boolean toqueTela = Gdx.input.justTouched();            //verifica toque do jogador

        if(estadoJogo == 0)   {                                 //estado inicial do jogo, mudado pelo toque do jogador

            posicaoCoinHorizontal = larguraDispositivo / 2;

            if (Gdx.input.justTouched()) {                     //gera força para cima no jogador sempre que a tela for tocada
                gravidade = -15;                               //efeito da gravidade ("puxar" o jogador para baixo)
                estadoJogo = 1;                                //altera o estado do jogo
                somVoando.play();                              //toca o som de voo quando a tela é tocada
            }
        } else if (estadoJogo == 1){                           //se o estado do jogo mudar, é possivel tocar mais vezes para voar

            if (Gdx.input.justTouched()) {                     //impulsiona pra cima
                gravidade = -15;                               //efeito da gravidade ("puxar" o jogador para baixo)
                somVoando.play();                              //toca o som de voo quando a tela é tocada
            }

            posicaoCanoHorizontal -= Gdx.graphics.getDeltaTime() * 200;       //velocidade do cano para vir até o jogador

            posicaoCoinHorizontal -= Gdx.graphics.getDeltaTime() * 200;       //velocidade da moeda para vir até o jogador

            if(posicaoCoinHorizontal < - ouroCoin.getWidth())
            {
                posicaoCoinHorizontal = larguraDispositivo;
                posicaoCoinVertical = random.nextInt(200) - 200;              //randomiza a distancia
            }

            if(posicaoCoinHorizontal < - prataCoin.getWidth())
            {
                posicaoCoinHorizontal = larguraDispositivo;
                posicaoCoinVertical = random.nextInt(200) - 200;              //randomiza a distancia
            }


            if(posicaoCanoHorizontal < - canoBaixo.getWidth()){
                posicaoCanoHorizontal = larguraDispositivo;
                posicaoCanoVertical = random.nextInt(400) -200;               //randomiza a distancia
                passouCano = false;                                           //volta para false
            }

            if (posicaoInicialVerticalPassaro > 0 || toqueTela)               //liga a gravidade ao toque
                posicaoInicialVerticalPassaro = posicaoInicialVerticalPassaro - gravidade;

            gravidade++;                                                      //aumenta a gravidade

        } else if (estadoJogo == 2){                                          //caso o estado seja 2

            if(pontos > pontuacaoMaxima){
                pontuacaoMaxima = pontos;                                     //highscore = pontos
                preferencias.putInteger("pontuacaoMaxima", pontuacaoMaxima);  //armazena o highscore para manter salvo
            }

            posicaoHorizontalPassaro -= Gdx.graphics.getDeltaTime() * 500;    //recuo do passaro quando ele bate

            if(toqueTela){                                                    //quando a tela é tocada
                estadoJogo = 0;                                               //estado do jogo se torna 0
                pontos = 0;                                                   //pontos vão para 0
                gravidade = 0;                                                //gravidade se torna 0
                posicaoHorizontalPassaro = 0;                                 //para que não haja erro na posição do jogador
                posicaoInicialVerticalPassaro = alturaDispositivo / 2;        //retorna o jogador para a posição inicial
                posicaoCanoHorizontal = larguraDispositivo;                   //reinicia os canos
            }
        }
    }

    private void desenharTexturas() {

        batch.setProjectionMatrix(camera.combined);      //ajusta a camera a resolução do celular


        batch.begin();                                  //inicia



        batch.draw(fundo, 0, 0, larguraDispositivo, alturaDispositivo);                                                                                                                 //gera o background de acordo com a resolução do celular passada pelos parametros
        batch.draw(passaros[(int) variacao], 50 + posicaoHorizontalPassaro, posicaoInicialVerticalPassaro);                                                                             //gera o jogador com suas imagens e animações

        batch.draw(canoBaixo, posicaoCanoHorizontal, alturaDispositivo / 2 - canoBaixo.getHeight() - espaçoEntreCanos / 2 + posicaoCanoVertical);                                       //gera o cano na tela com a distância entre eles
        batch.draw(canoTopo, posicaoCanoHorizontal, alturaDispositivo / 2 + espaçoEntreCanos / 2 + posicaoCanoVertical);                                                                //gera o cano na tela com a distância entre eles

        batch.draw(ouroCoin, posicaoCoinHorizontal, alturaDispositivo / 2 + posicaoCoinVertical);
        batch.draw(prataCoin, posicaoCoinHorizontal , alturaDispositivo / 2 + posicaoCoinVertical + 200);

        textoPontuacao.draw(batch,String.valueOf(pontos), larguraDispositivo / 2, alturaDispositivo - 100);

        if(estadoJogo == 0){
            batch.draw(flappyLogo, larguraDispositivo / 2 +200 - flappyLogo.getWidth(), alturaDispositivo / 2);
            textoIniciar.draw(batch, "TOQUE NA TELA PARA INICIAR!", larguraDispositivo / 2 -200, alturaDispositivo / 2 - flappyLogo.getHeight() / 2);                                    //gera o texto de reiniciar de acordo com os parametros
        }

        if(estadoJogo == 2){                 //caso o estado do jogo seja 2
            batch.draw(gameOver, larguraDispositivo / 2 +200 - gameOver.getWidth(), alturaDispositivo / 2);                                                                             //gera o game over de acordo com os parametros
            textoReiniciar.draw(batch, "TOQUE NA TELA PARA REINICIAR!", larguraDispositivo / 2 -250, alturaDispositivo / 2 - gameOver.getHeight() / 2);                                 //gera o texto reiniciar
            textoMelhorPontuacao.draw(batch, "SUA MELHOR PONTUAÇÃO É:" + pontuacaoMaxima + "PONTOS", larguraDispositivo / 2 -250, alturaDispositivo / 2 - gameOver.getHeight() * 2);    //gera o texto highscore
        }

        batch.end();   //acaba a execução

    }

    @Override
    public void resize(int width, int height){                                    //metodo para ajustar o tamanho da tela
        viewport.update(width, height);                                           //update sempre verifica a resolução
    }

    private void validarPontos() {

        if(posicaoCanoHorizontal < 50 - passaros[0].getWidth()){                  //quando passa pelo cano
           if(!passouCano){                                                       //checa se passou pelo cano
               pontos++;                                                          //aumenta a pontuação
               passouCano = true;                                                 //se tiver passado pelo cano, se torna true
               somPontuacao.play();                                               //toca o som quando passa pelo cano
           }
        }

        variacao += Gdx.graphics.getDeltaTime() * 10;
        if (variacao > 3)                                         //varia a animação
            variacao = 0;
    }
    private void detectarColisão() {

        circuloPassaro.set(50 + passaros[0].getWidth() / 2, posicaoInicialVerticalPassaro + passaros[0].getHeight() / 2, passaros[0].getWidth() / 2);                                               //associa collider ao passaro
        retanguloCanoBaixo.set(posicaoCanoHorizontal, alturaDispositivo / 2 - canoBaixo.getHeight() - espaçoEntreCanos / 2 + posicaoCanoVertical, canoBaixo.getWidth(), canoBaixo.getHeight());     //associa collider ao cano baixo
        retanguloCanoCima.set(posicaoCanoHorizontal, alturaDispositivo / 2 + espaçoEntreCanos / 2 + posicaoCanoVertical, canoTopo.getWidth(), canoTopo.getHeight() );                               //associa collider ao cano topo
        circuloCoinOuro.set(ouroCoin.getHeight(), 300 + posicaoCoinVertical + ouroCoin.getHeight() / 2, ouroCoin.getWidth() );                                                                      //associa collider a moeda de ouro
        circuloCoinPrata.set(prataCoin.getHeight(), 300 + posicaoCoinVertical + ouroCoin.getHeight() / 2, prataCoin.getWidth() );                                                                   //associa collider a moeda de prata

        boolean colisaoCanoCima = Intersector.overlaps(circuloPassaro, retanguloCanoCima);           //verifica se bateu no cano(cima)
        boolean colisaoCanoBaixo = Intersector.overlaps(circuloPassaro, retanguloCanoBaixo);         //verifica se bateu no cano(baixo)

        boolean colisaoCoinPrata = Intersector.overlaps(circuloPassaro, circuloCoinPrata);           //verifica se bateu na moeda(prata)
        boolean colisaoCoinOuro = Intersector.overlaps(circuloPassaro, circuloCoinOuro);             //verifica se bateu na moeda(ouro)

        if(colisaoCanoBaixo || colisaoCanoCima){                          //mensagem se bater no cano
            Gdx.app.log("log", "Colidiu");
           if(estadoJogo == 1) {
               somColisão.play();                                        //toca o som de colisão quando colidir
               estadoJogo = 2;                                           //muda o estado para 2
           }
        }

        if(colisaoCoinOuro){
            somCoin.play();                //toca o som de pegar moeda
            pontos = pontos + 10;          //aumenta a pontuação
        }
        if(colisaoCoinPrata){
            pontos = pontos + 5;          //aumenta a pontuação
            somCoin.play();               //toca o som de pegar moeda
        }
    }
}
