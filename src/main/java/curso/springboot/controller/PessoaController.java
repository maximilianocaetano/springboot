package curso.springboot.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import curso.springboot.model.Pessoa;
import curso.springboot.model.Telefone;
import curso.springboot.repository.PessoaRepository;
import curso.springboot.repository.ProfissaoRepository;
import curso.springboot.repository.TelefoneRepository;

@Controller
public class PessoaController {
	
	@Autowired
	private PessoaRepository pessoaRepository;
	
	@Autowired
	private TelefoneRepository telefoneRepository;
	
	@Autowired
	private ReportUtil reportUtil;
	
	@Autowired
	private ProfissaoRepository profissaoRepository;

	@RequestMapping(method = RequestMethod.GET, value = "**/cadastropessoa")
	public ModelAndView inicio() {
		
		
		ModelAndView modelandView = new ModelAndView("cadastro/cadastropessoa");
		modelandView.addObject("pessoaobj",new Pessoa());		
		modelandView.addObject("pessoas",pessoaRepository.findAll(PageRequest.of(0, 5, Sort.by("nome"))));
		//Iterable<Pessoa> pessoasIt = pessoaRepository.findAll();
		modelandView.addObject("profissoes",profissaoRepository.findAll());
		//modelandView.addObject("pessoas",pessoasIt);
		return modelandView;
	}
	
	@GetMapping("/pessoaspag")
	public ModelAndView carregaPessoasPorPaginacao(@PageableDefault(size = 5) Pageable pageable,
			ModelAndView model, @RequestParam("nomepesquisa") String nomepesquisa) {
		
		Page<Pessoa> pagePessoa = pessoaRepository.findPessoaByNamePage(nomepesquisa, pageable);
		model.addObject("pessoas",pagePessoa);
		model.addObject("pessoaobj", new Pessoa());
		model.addObject("nomepesquisa",nomepesquisa );
		model.setViewName("cadastro/cadastropessoa");
		return model;
		
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "**/salvarpessoa", consumes = {"multipart/form-data"})
	public ModelAndView salvar(@Valid Pessoa pessoa, BindingResult bindingResult, final MultipartFile file) throws IOException {		
		
		System.out.println(file.getContentType());
		System.out.println(file.getOriginalFilename());
		
		pessoa.setTelefones(telefoneRepository.getTelefones(pessoa.getId()));
		
		if(bindingResult.hasErrors()) {
			ModelAndView modelAndView = new ModelAndView("cadastro/cadastropessoa");
			modelAndView.addObject("pessoas",pessoaRepository.findAll(PageRequest.of(0, 5, Sort.by("nome"))));		
			modelAndView.addObject("pessoaobj",pessoa);
			
			List<String> msg = new ArrayList<String>();
			for(ObjectError objectError : bindingResult.getAllErrors()) {
				msg.add(objectError.getDefaultMessage());
			}
			
			modelAndView.addObject("profissoes",profissaoRepository.findAll());
			modelAndView.addObject("msg", msg);
			return modelAndView;
			
		}
		
		if(file.getSize() >0) {
			pessoa.setCurriculo(file.getBytes());
			pessoa.setTipoFileCurriculo(file.getContentType());
			pessoa.setNomeFileCurriculo(file.getOriginalFilename());
		}else {
			if(pessoa.getId() != null && pessoa.getId() >0) {
				Pessoa pessoaTemp = pessoaRepository.findById(pessoa.getId()).get();
				
				pessoa.setCurriculo(pessoaTemp.getCurriculo());
				
				pessoa.setTipoFileCurriculo(pessoaTemp.getTipoFileCurriculo());
				pessoa.setNomeFileCurriculo(pessoaTemp.getNomeFileCurriculo());
			}
		}
		pessoaRepository.save(pessoa);	
		
		ModelAndView andView = new ModelAndView("cadastro/cadastropessoa");
		andView.addObject("pessoas",pessoaRepository.findAll(PageRequest.of(0, 5, Sort.by("nome"))));		
		andView.addObject("pessoaobj",new Pessoa());
		
		return andView;
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/listapessoas")
	public ModelAndView pessoas() {
		
		ModelAndView andView = new ModelAndView("cadastro/cadastropessoa");
		andView.addObject("pessoas",pessoaRepository.findAll(PageRequest.of(0, 5, Sort.by("nome"))));
		
		andView.addObject("pessoaobj",new Pessoa());
		return andView;
	}
	
	@GetMapping("/editarpessoa/{idpessoa}")
	public ModelAndView editar(@PathVariable("idpessoa") Long idpessoa) {
		
		Optional<Pessoa> pessoa = pessoaRepository.findById(idpessoa);
		
		ModelAndView modelandView = new ModelAndView("cadastro/cadastropessoa");
		modelandView.addObject("pessoaobj",pessoa.get());
		modelandView.addObject("profissoes",profissaoRepository.findAll());
		
		return modelandView;
		
	}
	
	@GetMapping("/removerpessoa/{idpessoa}")
	public ModelAndView excluir(@PathVariable("idpessoa") Long idpessoa) {
		
		pessoaRepository.deleteById(idpessoa);
		
		ModelAndView modelandView = new ModelAndView("cadastro/cadastropessoa");
		modelandView.addObject("pessoas",pessoaRepository.findAll(PageRequest.of(0, 5, Sort.by("nome"))));
		modelandView.addObject("pessoaobj",new Pessoa());
		
		return modelandView;
		
	}
	
	@GetMapping("**/baixarcurriculo/{idpessoa}")
	public void baixarcurriculo(@PathVariable("idpessoa") Long idpessoa,
			HttpServletResponse response) throws Exception {
		
		Pessoa pessoa = pessoaRepository.findById(idpessoa).get();
		if(pessoa.getCurriculo()!=null) {
			
			response.setContentLength(pessoa.getCurriculo().length);
			
			response.setContentType(pessoa.getTipoFileCurriculo());
			
			String headerKey = "Content-Disposition";
			String headerValue = String.format("attachment; filename=\"%s\"", pessoa.getNomeFileCurriculo());
			response.setHeader(headerKey, headerValue);
			
			response.getOutputStream().write(pessoa.getCurriculo());
			
		}
		
	}
	
	@PostMapping("**/pesquisarpessoa")
	public ModelAndView pesquisar(@RequestParam("nomepesquisa") String nomepesquisa,
			                      @RequestParam("pesqsexo") String pesqsexo, @PageableDefault(size = 5, sort = {"nome"})
	                              Pageable pageable) {
		
		Page<Pessoa> pessoas = null;
		
		if(pesqsexo !=null && !pesqsexo.isEmpty()) {
			pessoas = pessoaRepository.findPessoaBySexoPage(nomepesquisa, pesqsexo,pageable);
			
		}else {
			pessoas = pessoaRepository.findPessoaByNamePage(nomepesquisa,pageable);
		}
		
		ModelAndView modelandView = new ModelAndView("cadastro/cadastropessoa");
		modelandView.addObject("pessoas",pessoas);
		modelandView.addObject("pessoaobj",new Pessoa());
		modelandView.addObject("nomepesquisa",nomepesquisa);
		return modelandView;
		
	}
	@GetMapping("**/pesquisarpessoa")
	public void imprimePdf(@RequestParam("nomepesquisa") String nomepesquisa, 
			@RequestParam("pesqsexo") String pesqsexo,
			HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		
		List<Pessoa> pessoas = new ArrayList<Pessoa>();
		
		if (pesqsexo != null && !pesqsexo.isEmpty()
				&& nomepesquisa != null && !nomepesquisa.isEmpty()) {/*Busca por nome e sexo*/
			
			pessoas = pessoaRepository.findPessoaByNameSexo(nomepesquisa, pesqsexo);
			
		}else if (nomepesquisa != null && !nomepesquisa.isEmpty()) {/*Busca somente por nome*/
			
			pessoas = pessoaRepository.findPessoaByName(nomepesquisa);
			
		}else if(pesqsexo !=null && !pesqsexo.isEmpty()) {
			
			pessoas = pessoaRepository.findPessoaBySexo(pesqsexo);
		}
	  
		else {/*Busca todos*/
			
			Iterable<Pessoa> iterator = pessoaRepository.findAll();
			for (Pessoa pessoa : iterator) {
				pessoas.add(pessoa);
			}
		}
		
		/*Chame o serviço que faz a geração do relatorio*/
		byte[] pdf = reportUtil.gerarRelatorio(pessoas, "pessoa", request.getServletContext());
		
	    /*Tamanho da resposta*/
		response.setContentLength(pdf.length);
		
		/*Definir na resposta o tipo de arquivo*/
		response.setContentType("application/octet-stream");
		
		/*Definir o cabeçalho da resposta*/
		String headerKey = "Content-Disposition";
		String headerValue = String.format("attachment; filename=\"%s\"", "relatorio.pdf");
		response.setHeader(headerKey, headerValue);
		
		/*Finaliza a resposta pro navegador*/
		response.getOutputStream().write(pdf);
		
	}
	
	@PostMapping("**/addfonePessoa/{pessoaid}")
	public ModelAndView addFonePessoa(Telefone telefone, @PathVariable("pessoaid") Long pessoaid) {
		
		Pessoa pessoa = pessoaRepository.findById(pessoaid).get();
		
		if(telefone!= null && telefone.getNumero().isEmpty() || telefone.getTipo().isEmpty()) {
			
			ModelAndView modelandView = new ModelAndView("cadastro/telefones");
			modelandView.addObject("pessoaobj",pessoa);
			modelandView.addObject("telefones",telefoneRepository.getTelefones(pessoaid));
			
			List<String> msg = new ArrayList<String>();
			
			if(telefone.getNumero().isEmpty()) {
				msg.add("Numero deve ser informado");
			}
			
			if(telefone.getTipo().isEmpty()) {
				msg.add("Tipo deve ser informado");
			}
			modelandView.addObject("msg",msg);
			
			return modelandView;
			
		}
		
		ModelAndView modelandView = new ModelAndView("cadastro/telefones");
		
		telefone.setPessoa(pessoa);
		
		telefoneRepository.save(telefone);
		
		modelandView.addObject("pessoaobj",pessoa);
		modelandView.addObject("telefones",telefoneRepository.getTelefones(pessoaid));
		
		return modelandView;	
	
  }
	
	@GetMapping("/removertelefone/{idtelefone}")
	public ModelAndView removertelefone(@PathVariable("idtelefone") Long idtelefone) {
		
		Pessoa pessoa = telefoneRepository.findById(idtelefone).get().getPessoa();
		
		telefoneRepository.deleteById(idtelefone);
		
		ModelAndView modelandView = new ModelAndView("cadastro/telefones");
		modelandView.addObject("pessoaobj",pessoa);
		modelandView.addObject("telefones",telefoneRepository.getTelefones(pessoa.getId()));

		
		return modelandView;
		
	}
}
