# Zeladoria Urbana Participativa - Aplicativo Cidadão Android

## Introdução

Sabemos que o manejo de informação é uma das chaves para uma gestão eficiente, para isso o ZUP apresenta um completo histórico de vida de cada um dos ativos e dos problemas do município, incorporando solicitacões de cidadãos, dados georeferenciados, laudos técnicos, fotografias e ações preventivas realizadas ao longo do tempo. Desta forma, o sistema centraliza todas as informações permitindo uma rápida tomada de decisões tanto das autoridades como dos técnicos em campo.

O componente **ZUP Cidadão Android** atua como ferramenta de contato com o público e a gestão da instituição que está utilizando o ZUP.

## Tecnologias

O ZUP Técnico Android utiliza a versão 22 do SDK do Android, além de diversas bibliotecas.

## Instalação

Para instalar o projeto, o modo recomendado é [baixar a última versão do Android Studio](https://developer.android.com/sdk/index.html) e importar o diretório.

## Backend

É necessário o componente ZUP-API estar rodando em seu servidor.

Você precisará configurar o endereço do mesmo no arquivo `src/main/java/br/com/lfdb/zup/core/Constantes.java`, modificando a constante `REST_URL` para o endereço correto do seu servidor.

==========================
Zup Técnico
==========================

# Lista de Build Types
* debug
* release

# Principais comandos gradlew (Está na pasta do projeto o arquivo **gradlew** ou **gradlew.bat**)
* ``assemble``
* ``clean``
* ``install``

# Comandos para o uso no projeto
* ``./gradlew clean assemble<Flavor><BuildType>`` (Ex: ``./gradlew clean assembleStagingfRelease``):  Limpa o projeto e gera apk com o flavor e o build type na pasta **app/build/outputs/apk/**. Se o somente o flavor for fornecido, ele gera todos os apks daquele flavor. Se nem o flavor for fornecido, ele gera todas as apks possíveis.
* ``./gradlew assemble<Flavor><BuildType> crashlyticsUploadDistribution<Flavor><BuildType>`` (Ex: ``./gradlew assembleStagingRelease crashlyticsUploadDistributionStagingRelease``): Gera o apk e envia a apk ao Crashlytics de acordo com o Flavor dado (cada flavor tem um pacote identificador que será relacionado ao projeto do Fabric.io) e convida os usuários definidos em FABRIC_EMAILS para usar o app.

# Dados de autenticação/configuração Crashlytics e keystore
* Arquivo ``gradle.properties``
