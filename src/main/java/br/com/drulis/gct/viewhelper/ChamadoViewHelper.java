package br.com.drulis.gct.viewhelper;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import br.com.drulis.gct.command.ConsultarCommand;
import br.com.drulis.gct.core.Acao;
import br.com.drulis.gct.dominio.Atividade;
import br.com.drulis.gct.dominio.Chamado;
import br.com.drulis.gct.dominio.Contato;
import br.com.drulis.gct.dominio.DominioInterface;
import br.com.drulis.gct.dominio.Mensagem;
import br.com.drulis.gct.util.Resultado;

/**
 * 
 * @author Victor Drulis
 * @since 23 de mar de 2019
 * @contact victordrulis@gmail.com
 *
 */
public class ChamadoViewHelper implements ViewHelperInterface {

    @Override
    public DominioInterface getData(HttpServletRequest request) {
        ConsultarCommand consultar = new ConsultarCommand();
        String acao = request.getParameter("acao");
        Chamado chamado = new Chamado();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        
        String[] listaAtividadeId = request.getParameterValues("atividade");
        String titulo = request.getParameter("titulo");
        String status = request.getParameter("status");
        String dataAbertura = request.getParameter("dataAbertura");
        String dataFechamento = request.getParameter("dataFechamento");
        String ativo = request.getParameter("ativo");
        
        System.out.println("[" + this.getClass().getSimpleName() + "] --getData, ACAO: " + acao + ", URI: " + request.getRequestURI());

        if(acao == null) {
            acao = Acao.LISTAR.getAcao();
        }
        
        if(!acao.equals(Acao.SALVAR.getAcao()) && !acao.equals(Acao.NOVO.getAcao())) {
            String id = request.getParameter("id");
            
            if(id != null && id != "")
                chamado.setId(Integer.parseInt(id));
        }
        
        if(acao.equals(Acao.SALVAR.getAcao()) || acao.equals(Acao.ALTERAR.getAcao()) || acao.equals(Acao.EDITAR.getAcao()) && request.getMethod().equals("POST")) {
            
            if (listaAtividadeId != null){
                for(String atividadeId : listaAtividadeId) {
                    Atividade p = new Atividade();
                    p.setId(Integer.parseInt(atividadeId));
                    chamado.getListaAtividades().add((Atividade) consultar.execute(p).getEntidades().get(0));
                }
            }
            
            chamado.setTitulo(titulo);
            chamado.setStatus(Integer.parseUnsignedInt(status));

            if(ativo != null || ativo != "0")
                chamado.setAtivo(1);
        }
        
        if(!acao.equals(Acao.EXCLUIR.getAcao()) && request.getMethod().equals("GET")) {
            try {
                
                if (listaAtividadeId != null){
                    for(String atividadeId : listaAtividadeId) {
                        Atividade p = new Atividade();
                        p.setId(Integer.parseInt(atividadeId));
                    }
                }
                
                chamado.setTitulo(titulo);
                chamado.setStatus((status != null) ? Integer.parseUnsignedInt(status) : 0);
                chamado.setDataAbertura((dataAbertura != null) ? dateFormat.parse(dataAbertura) : new Date());
                chamado.setDataFechamento((dataFechamento != null) ? dateFormat.parse(dataFechamento) : new Date());
            } catch (ParseException e) {
                System.out.println("[" + this.getClass().getSimpleName() + "] " + Mensagem.ERRO_CONVERTER_DADOS.getDescricao() + "; \n" + e.getMessage());
                e.printStackTrace();
            }
            
            if(ativo != null || ativo != "0")
                chamado.setAtivo(1);
        }
        
        return chamado;
    }

    @Override
    public void setView(Resultado resultado, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        ConsultarCommand consultar = new ConsultarCommand();
        Resultado resContato = new Resultado();
        Resultado resAtividade = new Resultado();
        
        try {
            resAtividade = consultar.execute(new Atividade());
            resContato = consultar.execute(new Contato());
        } catch (Exception e) {
            System.out.println("[" + this.getClass().getSimpleName() + "] " + Mensagem.ERRO_CONVERTER_DADOS + ": " + e.getMessage());
            e.printStackTrace();
        }
        
        String mensagem = null;
        String uri = request.getRequestURI();
        String acao = request.getParameter("acao");
        
        System.out.println("[" + this.getClass().getSimpleName() + "] setView: Acao = " + acao + ", URI: " + uri);
        
        if(resultado != null) {
            mensagem = resultado.getMensagem();
        }
        
        if(acao != null && acao.equals(Acao.NOVO.getAcao())) {
            request.setAttribute("listaContato", resContato.getEntidades());
            request.setAttribute("listaAtividade", resAtividade.getEntidades());
            request.getRequestDispatcher("/jsp/chamado/form.jsp").forward(request, response);
        } else {
            switch(resultado.getEntidades().size()) {
            case 0:
                request.setAttribute("listaContato", resContato.getEntidades());
                request.setAttribute("listaAtividade", resAtividade.getEntidades());
                request.getRequestDispatcher("/jsp/chamado/form.jsp").forward(request, response);
                break;
            
            case 1:
                request.setAttribute("resultado", resultado.getEntidades().get(0));
                
                if(acao != null && acao.equals(Acao.EDITAR.getAcao())) {
                    request.getRequestDispatcher("/jsp/chamado/edit.jsp").forward(request, response);
                    break;
                }
                
                if(acao != null && acao.equals(Acao.EXIBIR.getAcao())) {
                    request.getRequestDispatcher("/jsp/chamado/show.jsp").forward(request, response);
                    break;
                }
                
                request.setAttribute("resultado", resultado.getEntidades());
                request.getRequestDispatcher("/jsp/chamado/index.jsp").forward(request, response);
                break;
            
            default:
                if(mensagem != null && !mensagem.equals("")) {
                    request.setAttribute("mensagem", mensagem);
                    request.getRequestDispatcher("mensagem.jsp").forward(request, response);
                    break;
                }
                
                if(resultado.getEntidades() == null || resultado.getEntidades().size() < 1) {
                    request.setAttribute("mensagem", Mensagem.ERRO_NAO_ENCONTRADO.getDescricao());
                    request.getRequestDispatcher("mensagem.jsp").forward(request, response);
                    break;
                }
                
                request.setAttribute("resultado", resultado.getEntidades());
                request.getRequestDispatcher("/jsp/chamado/index.jsp").forward(request, response);
                break;
            }
        }
        
    }

}
