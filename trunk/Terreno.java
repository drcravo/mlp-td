/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mlptd;

import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.util.Color;

/**
 * Um terreno contém um conjunto de tiles, um caminho e um conjunto de inimigos.
 * @author diogo
 */
public class Terreno extends Desenho {
    /**
     * Array de tiles do terreno.
     * A posição da tile no array deve refletir sua posição no terreno.
     * Assim, tiles[0][0] é a tile mais acima e à esquerda.
     */
    Tile[][] tiles;

    /**
     * Caminho composto por índices de tiles em tiles.
     * O caminho mostra um lugar por onde os inimigos podem movimentar-se.
     */
    Caminho caminho;

    /**
     * O tamanho que cada tile deve ter.
     */
    float comprimentoCadaTile;
    float larguraCadaTile;

    /**
     * Dimensões do terreno em tiles.
     */
    int tilesPorLinha;
    int tilesPorColuna;

    /**
     * O terreno é criado com um número especificado de tiles de forma que ocupe as dimensões que lhe são especificadas.
     * @param _posicao Posição do terreno.
     * @param _dimensoesTela Comprimento e largura do terreno em unidades de tela.
     * @param _dimensoesTiles Comprimento e largura do terreno em tiles.
     */
     public Terreno(float _posX, float _posY, float _comprimentoTela, float _larguraTela,
                    int _comprimentoEmTiles, int _larguraEmTiles){
        comprimentoCadaTile = _comprimentoTela/_comprimentoEmTiles;
        larguraCadaTile = _larguraTela/_larguraEmTiles;
        
        tilesPorLinha = _comprimentoEmTiles;
        tilesPorColuna = _larguraEmTiles;

		super(_posX, _posY+_larguraTela, _comprimentoTela, _larguraTela, 100);
		
        tiles = new Tile[tilesPorLinha][tilesPorColuna];

        //Pequeno teste. Notar que a tile (0,0) é sinalizada com verde, e não laranja.
        transformarEmTabuleiro(new Color(Color.ORANGE));
        //transformarEmTabuleiro(new Color(Color.GREEN));

        caminho = Caminho.criarCaminhoDiagonalDecrescente(tilesPorColuna, tilesPorLinha, tilesPorColuna);
        transformarTerrenoSegundoCaminho(caminho);

        posicao = 0;
     }

     /**
      * Move os inimigos que estão neste terreno.
      * Os inimigos seguem a rota indicada pelo caminho.
      */
     int posicao;
     public void moverInimigos() throws InterruptedException{
        Inimigo inimigoExemplo = new Inimigo();
        TilePassadouro tile;
        int linha;
        int coluna;
        Thread thisThread = Thread.currentThread();
        if(posicao < caminho.getComprimento()){
            if(0 < posicao){
                coluna = caminho.getColunaTile(posicao-1);
                linha = caminho.getLinhaTile(posicao-1);
                tile = (TilePassadouro) tiles[coluna][linha];
                tile.retirarTodosInimigos();
            }
            coluna = caminho.getColunaTile(posicao);
            linha = caminho.getLinhaTile(posicao);
            tile = (TilePassadouro) tiles[coluna][linha];
            tile.adicionarInimigo(inimigoExemplo);
            thisThread.sleep(1000);
            posicao++;
         }
     }


     /**
      * Aplica o caminho a este terreno, isto é, transforma as tiles que estão no caminho em TilePassadouro.
      * @param _caminho O caminho a ser aplicado.
      */
     private void transformarTerrenoSegundoCaminho(Caminho _caminho){
        int posicao;
        int linha;
        int coluna;
        Tile tileSubstituida;
        for(posicao = 0; posicao<_caminho.getComprimento(); posicao++){
            coluna = _caminho.getColunaTile(posicao);
            linha = _caminho.getLinhaTile(posicao);
            tileSubstituida = tiles[coluna][linha];
            tiles[coluna][linha] = new TilePassadouro(tileSubstituida);
        }
     }

     /**
      * Transforma este terreno em um tabuleiro.
      * A casa de índice (0,0) é sinalizada com a cor de parâmetro.
      */
     private void transformarEmTabuleiro(Color _cor){
        int linha;
        int coluna;
        Color cor;
        for(coluna=0; coluna<tilesPorLinha; coluna++){
            for(linha=0; linha<tilesPorColuna; linha++){
                if(linha==0 && coluna == 0){
                    cor = new Color(_cor);
                } else if((coluna%2==0 && linha%2==1)
                   || (coluna%2==1 && linha%2==0)){
                    cor = new Color(Color.BLUE);
                } else {
                    cor = new Color(Color.RED);
                }
                adicionarTile(coluna, linha, cor);
            }
        }
     }
     
     /**
      * Adiciona uma tile ao terreno. Notar que as tiles têm seu tamanho limitado dentro do terreno,
      * isto é, todas possuem o mesmo tamanho que é definido na construção do terreno.
      * Caso a posição da tile esteja fora dos limites, não fará nada.
      * @param _posicao Posição da tile na matriz de tiles (e no terreno).
      * @param _cor Cor da tile.
      */
     private void adicionarTile(int _colunaTile, int _linhaTile, Color _cor){
         float xTile;
         float yTile;
         Tile tileQueDeveSerAdicionada;
         boolean tileEstahForaDosLimites = (tilesPorLinha <= _colunaTile || tilesPorColuna <= _linhaTile);
         if(!tileEstahForaDosLimites){
            xTile = posX + _colunaTile*comprimentoCadaTile;
            yTile = posY - _linhaTile*larguraCadaTile;
            
            tileQueDeveSerAdicionada = new Tile(xTile, yTile, comprimentoCadaTile, larguraCadaTile);
            tileQueDeveSerAdicionada.mudarCor(_cor);
            tiles[_colunaTile][_linhaTile] = tileQueDeveSerAdicionada;
         }
     }
     
     /**
      * Desenha todas as tiles do terreno.
      */
     public void desenhar(){
         for(int linha=0; linha<tilesPorColuna; linha++){
            for(int coluna=0; coluna<tilesPorLinha; coluna++){
                tiles[coluna][linha].desenhar();
            }
         }
     }

     /**
      * Move o terreno.
      * @param _destino Posição para onde o terreno deve ir.
      */
     public void mover(float _posX, float _posY){
        posX = _posX;
        posY = _posY;

        float xTile;
        float yTile;
        for(int linha=0; linha<tilesPorColuna; linha++){
            for(int coluna=0; coluna<tilesPorLinha; coluna++){
                xTile = posX + coluna*comprimentoCadaTile;
                yTile = posY - linha*larguraCadaTile;
                tiles[coluna][linha].mover(xTile, yTile);
            }
        }
     }
}
