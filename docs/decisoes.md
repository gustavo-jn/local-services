# Decisões de implementação

## 01/11/2026 - Integração da página inicial independente

- A referência fornecida está em `site/`, na raiz do repositório. Seus arquivos foram preservados e não são usados diretamente como uma segunda aplicação.
- O conteúdo foi integrado à Home standalone existente, mantendo a estrutura visual e as imagens fornecidas.
- Estilos do documento (`html`, `body`) foram separados dos estilos encapsulados da Home. As variáveis do componente usam `:host`, permitindo que funcionem com o encapsulamento do Angular.
- As interações do script independente usam sinais e bindings Angular. Observadores, listeners e temporizadores são encerrados ao destruir a Home; os ciclos visuais param quando a seção sai de vista, quando a aba fica oculta ou quando o usuário pausa o movimento.
- Foi adicionado controle explícito de pausa e respeitada a preferência por movimento reduzido. Fotos e textos permanecem disponíveis sem suporte a IntersectionObserver.
- Cadastro e login não foram simulados nem ampliados nesta tarefa. Seus botões ficam desativados, com indicação de indisponibilidade, até implementar as rotas correspondentes.
- As mensagens sobre etapas técnicas do projeto foram substituídas por conteúdo público sobre a proposta da plataforma.
- As imagens originais totalizam 15.313.489 bytes. Cópias em WebP, limitadas a 1920 px de largura para o fundo e 1000 px para as demais imagens, totalizam 524.222 bytes: redução aproximada de 96,6%. Os originais não foram alterados.
- A primeira compilação mediu 22,43 kB de CSS encapsulado para a Home, acima do limite padrão. Para acomodar o layout fornecido, o orçamento `anyComponentStyle` foi alterado de 4/8 kB para 24/28 kB (aviso/erro). Após os ajustes finais, o build passou sem aviso desse orçamento. O orçamento total dos arquivos iniciais permaneceu em 500 kB/1 MB.
- Não foram adicionadas bibliotecas, fontes remotas ou dependências ao projeto. A Home continua utilizando as fontes de sistema do material fornecido.
