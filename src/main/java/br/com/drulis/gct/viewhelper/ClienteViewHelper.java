package br.com.drulis.gct.viewhelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import br.com.drulis.gct.core.Acao;
import br.com.drulis.gct.dominio.Contato;
import br.com.drulis.gct.dominio.Cliente;
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
public class ClienteViewHelper implements ViewHelperInterface {

    @Override
    public DominioInterface getData(HttpServletRequest request) {
        String acao = request.getParameter("acao");
        Cliente cliente = new Cliente();
        
        System.out.println(this.getClass().getName() + " --getData, ACAO = " + acao + ", URI: " + request.getRequestURI());

        if(acao == null) {
            acao = Acao.LISTAR.getAcao();
        }
        
        if(!acao.equals(Acao.LISTAR.getAcao()) && !acao.equals(Acao.SALVAR.getAcao()) && !acao.equals(Acao.NOVO.getAcao())) {
            String id = request.getParameter("id");
            
            if(id != null && id != "")
                cliente.setId(Integer.parseInt(id));
        }
        
        if(!acao.equals(Acao.EXCLUIR.getAcao()) || acao.equals(Acao.SALVAR.getAcao()) && request.getMethod().equals("GET")) {
//            cliente.getSla(Integer.parseInt(request.getAttribute("sla")));
            
            if(request.getParameter("ativo") != null)
                cliente.setAtivo(1);
        }
        
        if(acao.equals(Acao.SALVAR.getAcao()) && request.getMethod().equals("POST")) {
            
            if(request.getParameter("ativo") != null)
                cliente.setAtivo(1);
        }
        
        return cliente;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setView(Resultado resultado, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        List<Contato> listContato = new ArrayList<>();
        String mensagem = null;
        String uri = request.getRequestURI();
        String acao = request.getParameter("acao");
        
        System.out.println(this.getClass() + " -- setView: Acao = " + acao + ", URI: " + uri);
        
        if(resultado != null) {
            mensagem = resultado.getMensagem();
            listContato = (List<Contato>) (Object) resultado.getEntidades();
        }
        
        switch(listContato.size()) {
        case 0:
            request.getRequestDispatcher("/jsp/contato/form.jsp").forward(request, response);
            break;
        
        case 1:
            request.setAttribute("resultado", listContato.get(0));
            if(acao.equals(Acao.ALTERAR.getAcao())) {
                request.getRequestDispatcher("/jsp/contato/edit.jsp").forward(request, response);
                break;
            }
            request.getRequestDispatcher("/jsp/contato/show.jsp").forward(request, response);
            break;
        
        default:
            if(mensagem != null && !mensagem.equals("")) {
                request.setAttribute("mensagem", mensagem);
                request.getRequestDispatcher("mensagem.jsp").forward(request, response);
                break;
            }
            
            if(acao != null && acao.equals(Acao.NOVO.getAcao())) {
                request.getRequestDispatcher("/jsp/contato/form.jsp").forward(request, response);
                break;
            }
            
            if(listContato == null || listContato.size() < 1) {
                request.setAttribute("mensagem", Mensagem.ERRO_NAO_ENCONTRADO.getDescricao());
                request.getRequestDispatcher("mensagem.jsp").forward(request, response);
                break;
            }
            
            request.setAttribute("resultado", listContato);
            request.getRequestDispatcher("/jsp/contato/index.jsp").forward(request, response);
            break;
        }
        
    }

}
