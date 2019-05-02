/**
 * 
 */
package br.com.drulis.gct.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import br.com.drulis.gct.core.Entidade;
import br.com.drulis.gct.dominio.Mensagem;
import br.com.drulis.gct.dominio.Usuario;

/**
 * @author Victor Drulis Oliveira
 * @since 14 de fev de 2019
 * @contact victordrulis@gmail.com
 *
 */
public class UsuarioDao extends DaoEntidade {

    @Override
    public Entidade inserir(Entidade entidade) {
        System.out.println("[" + this.getClass().getSimpleName() + "] Inserir");
        PreparedStatement ps = null;
        Usuario usuario = (Usuario) entidade;
        StringBuilder sql = new StringBuilder();
        Timestamp dataInclusao = new Timestamp(System.currentTimeMillis());
        
        try {
            this.conectar();
            sessaoBD.setAutoCommit(false);
            sql.append("INSERT INTO usuario (login, cpf_cnpj, telefone, senha, ativo, usuario_inclusao_id, data_inclusao)");
            sql.append(" VALUES (?,?,?,?,?,?,?)");
            ps = sessaoBD.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, usuario.getLogin());
            ps.setString(4, usuario.getSenha());
            ps.setInt(5, 1);
//            ps.setInt(6, usuario.getUsuarioInclusao().getId());
            ps.setInt(6, 1);
            ps.setTimestamp(7, dataInclusao);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            
            while(rs.next()) {
                usuario.setId(rs.getInt(1));
            }
            
            sessaoBD.commit();
            System.out.println("[" + this.getClass().getSimpleName() + "] " + Mensagem.OK_INSERIR.getDescricao() +" id: " + usuario.getId());
            return usuario;
        } catch (SQLException e) {
            System.out.println("[" + this.getClass().getSimpleName() + "] " + Mensagem.ERRO_INSERIR.getDescricao() + "\n: " + e);
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            System.out.println("[" + this.getClass().getSimpleName() + "] " + Mensagem.ERRO_INSERIR.getDescricao() + "\n: " + e);
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Entidade alterar(Entidade entidade) {
        System.out.println("[" + this.getClass().getSimpleName() + "] Alterar");
        Usuario usuario = new Usuario();
        Usuario alterado = new Usuario();
        StringBuilder sql = new StringBuilder();
        PreparedStatement ps = null;

        alterado = (Usuario) entidade;

        try {
            this.conectar();
            sessaoBD.setAutoCommit(false);
            sql.append("UPDATE usuario SET ");
            sql.append("telefone = ?, ");
            sql.append("senha = ?, ");
            sql.append("ativo = ?, ");
            sql.append("usuario_alteracao_id = ? ");
            sql.append("WHERE id = ?");
            ps = sessaoBD.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS);
            ps.setString(2, alterado.getSenha());
            ps.setInt(3, alterado.getAtivo());
            ps.setInt(4, 1);
            ps.setInt(5, alterado.getId());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            while(rs.next()) {
                alterado.setId(rs.getInt(1));
            }
            sessaoBD.commit();
            System.out.println("[" + this.getClass().getSimpleName() + "] " + Mensagem.OK_ATUALIZAR.getDescricao() + ", id: " + usuario.getId());
            return alterado;
        } catch (SQLException e) {
            System.out.println("[" + this.getClass().getSimpleName() + "] " + Mensagem.ERRO_ATUALIZAR + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            System.out.println("[" + this.getClass().getSimpleName() + "] " + Mensagem.ERRO_ATUALIZAR + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Entidade> consultar(Entidade entidade) {
        System.out.println("[" + this.getClass().getSimpleName() + "] Consultar");
        PreparedStatement ps = null;
        Usuario usuario = (Usuario) entidade;
        
        if(usuario.getUsuarioInclusao() == null) {
            usuario.setUsuarioInclusao(new Usuario());
            usuario.setUsuarioUpdate(new Usuario());
            usuario.setUsuarioInativacao(new Usuario());
        }
        
        List<Entidade> listaUsuarios = new ArrayList<Entidade>();
        StringBuilder sql = new StringBuilder();

        sql.append("SELECT c.* FROM usuario c WHERE 1 = 1 ");
        
        try {
            this.conectar();

            if (usuario.getId() > 0) {
                sql.append(" AND c.usuario_id = " + usuario.getId());
            }

            ps = sessaoBD.prepareStatement(sql.toString());
            ResultSet resultado = ps.executeQuery();

            while (resultado.next()) {
                Usuario con = new Usuario();
                con.setId(resultado.getInt("c.usuario_id"));
                con.setLogin(resultado.getString("c.login"));
                con.setSenha(resultado.getString("c.pass"));
                con.setAtivo(resultado.getInt("c.ativo"));
                con.setDataInclusao(resultado.getDate("c.data_inclusao"));
                con.setDataAlteracao(resultado.getDate("c.data_alteracao"));
                con.setDataInativacao(resultado.getDate("c.data_inativacao"));
                
                /*
                con.getUsuarioInclusao().setId(resultado.getInt("c.usuario_inclusao_id"));
                con.getUsuarioUpdate().setId(resultado.getInt("c.usuario_alteracao_id"));
                con.getUsuarioInativacao().setId(resultado.getInt("c.usuario_inativacao_id"));
                */
                
                listaUsuarios.add(con);
            }
            System.out.println("[" + this.getClass().getSimpleName() + "] " + Mensagem.OK_CONSULTAR.getDescricao());
        } catch (SQLException e) {
            System.out.println("[" + this.getClass().getSimpleName() + "] " + Mensagem.ERRO_NAO_ENCONTRADO.getDescricao()+ "\n" + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("[" + this.getClass().getSimpleName() + "] " + Mensagem.ERRO_EXIBIR.getDescricao() + e.getMessage());
            e.printStackTrace();
        }
        return listaUsuarios;
    }

    @Override
    public Boolean excluir(Entidade entidade) {
        PreparedStatement ps = null;
        Usuario usuario = (Usuario) entidade;
        StringBuilder sql = new StringBuilder();
        
        System.out.println("[" + this.getClass().getSimpleName() + "] Excluir id: " + usuario.getId());
        
        try {
            this.conectar();
            this.sessaoBD.setAutoCommit(false);
            sql.append("UPDATE usuario SET ativo = 0 WHERE usuario_id = ?");
            ps = this.sessaoBD.prepareStatement(sql.toString());
            ps.setInt(1,  usuario.getId());
            ps.executeUpdate();
            this.sessaoBD.commit();
            return true;
        } catch(SQLException e) {
            System.out.println("[" + this.getClass().getSimpleName() + "] " + Mensagem.ERRO_EXCLUIR.getDescricao()+ " --- id: " + usuario.getId() + e.getMessage());
            return false;
        }
    }
}
