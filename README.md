# Travel Buddy

Projeto académico de integração de sistemas para apoiar o planeamento de viagens, reunindo pesquisa de voos, hotéis e meteorologia, autenticação Google e viagens guardadas.

## Executar localmente

Requisitos: Java 21, Docker e Docker Compose.

1. Copiar `.env.example` para `.env` e substituri os valores de exemplo pelas tuas credenciais.
2. Iniciar a base de dados: `docker compose up -d postgres`.
3. Iniciar a aplicação: `./mvnw spring-boot:run` (Windows: `mvnw.cmd spring-boot:run`).
4. Abrir <http://localhost:8080>.

Para iniciar a stack completa em Docker, configurar as variáveis de `.env` e executar `docker compose up --build`.

## Disclaimer

Este projeto foi desenvolvido para fins académicos e educativos. Não é um serviço oficial nem tem afiliação com Google, Geoapify, OpenWeatherMap, RapidAPI ou outros fornecedores referidos. A disponibilidade e utilização das APIs dependem dos respetivos termos e limites. Os dados de aeroportos podem estar incompletos ou desatualizados; preços e avaliações de hotéis podem ser estimativas ou placeholders. Confirma sempre informações e preços diretamente junto dos fornecedores antes de tomar decisões de viagem.

O código ainda não tem uma licença de reutilização. A licença dos dados de aeroportos é independente da licença do código; consulta [DATA_SOURCES.md](DATA_SOURCES.md).

Para comunicar vulnerabilidades, consultar [SECURITY.md](SECURITY.md). Não publicar credenciais: manter os valores reais apenas no `.env`, que está excluído do Git.
