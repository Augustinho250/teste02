<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Formulário de Subestação</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 20px;
        }
        form {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 10px;
        }
        label {
            margin-bottom: 5px;
        }
        input, select {
            padding: 8px;
            font-size: 14px;
        }
        button {
            grid-column: 1 / -1;
            padding: 10px;
            font-size: 16px;
            background-color: #4CAF50;
            color: white;
            border: none;
            cursor: pointer;
        }
        button:hover {
            background-color: #45a049;
        }
    </style>
</head>
<body>

<h1>Formulário de Inspeção de Subestação</h1>
<form>
    <label for="data">Data</label>
    <input type="date" id="data" name="data">

    <label for="projeto">Projeto</label>
    <input type="text" id="projeto" name="projeto">

    <label for="subestacao">Subestação</label>
    <input type="text" id="subestacao" name="subestacao">

    <label for="tensao">Tensão Nominal</label>
    <input type="text" id="tensao" name="tensao">

    <label for="fabricante">Fabricante</label>
    <input type="text" id="fabricante" name="fabricante">

    <label for="tipo">Tipo</label>
    <input type="text" id="tipo" name="tipo">

    <label for="data_fabricacao">Data de Fabricação</label>
    <input type="date" id="data_fabricacao" name="data_fabricacao">

    <label for="tempo">Tempo</label>
    <input type="text" id="tempo" name="tempo">

    <label for="temp_ambiente">Temperatura Ambiente</label>
    <input type="text" id="temp_ambiente" name="temp_ambiente">

    <label for="ura">URA</label>
    <input type="text" id="ura" name="ura">

    <label for="nserie_fase_a">N. Série Fase A</label>
    <input type="text" id="nserie_fase_a" name="nserie_fase_a">

    <label for="nserie_fase_b">N. Série Fase B</label>
    <input type="text" id="nserie_fase_b" name="nserie_fase_b">

    <label for="nserie_fase_v">N. Série Fase V</label>
    <input type="text" id="nserie_fase_v" name="nserie_fase_v">

    <label for="nserie_fase_r">N. Série Fase R</label>
    <input type="text" id="nserie_fase_r" name="nserie_fase_r">

    <label for="r_isolamento_a">R. Isolamento Fase A (1min)</label>
    <input type="text" id="r_isolamento_a" name="r_isolamento_a">

    <label for="r_isolamento_b">R. Isolamento Fase B (1min)</label>
    <input type="text" id="r_isolamento_b" name="r_isolamento_b">

    <label for="r_isolamento_v">R. Isolamento Fase V (1min)</label>
    <input type="text" id="r_isolamento_v" name="r_isolamento_v">

    <label for="r_isolamento_r">R. Isolamento Fase R (1min)</label>
    <input type="text" id="r_isolamento_r" name="r_isolamento_r">

    <label for="instrumento">Instrumento Utilizado</label>
    <input type="text" id="instrumento" name="instrumento">

    <label for="modelo">Modelo</label>
    <input type="text" id="modelo" name="modelo">

    <label for="nserie">N. Série</label>
    <input type="text" id="nserie" name="nserie">

    <label for="data_afericao">Data de Aferição</label>
    <input type="date" id="data_afericao" name="data_afericao">

    <label for="contador_a_encontrado">N. Contador de Descarga Fase A Encontrado</label>
    <input type="text" id="contador_a_encontrado" name="contador_a_encontrado">

    <label for="contador_a_deixado">N. Contador de Descarga Fase A Deixado</label>
    <input type="text" id="contador_a_deixado" name="contador_a_deixado">

    <label for="contador_b_encontrado">N. Contador de Descarga Fase B Encontrado</label>
    <input type="text" id="contador_b_encontrado" name="contador_b_encontrado">

    <label for="contador_b_deixado">N. Contador de Descarga Fase B Deixado</label>
    <input type="text" id="contador_b_deixado" name="contador_b_deixado">

    <label for="contador_v_encontrado">N. Contador de Descarga Fase V Encontrado</label>
    <input type="text" id="contador_v_encontrado" name="contador_v_encontrado">

    <label for="contador_v_deixado">N. Contador de Descarga Fase V Deixado</label>
    <input type="text" id="contador_v_deixado" name="contador_v_deixado">

    <label for="contador_r_encontrado">N. Contador de Descarga Fase R Encontrado</label>
    <input type="text" id="contador_r_encontrado" name="contador_r_encontrado">

    <label for="contador_r_deixado">N. Contador de Descarga Fase R Deixado</label>
    <input type="text" id="contador_r_deixado" name="contador_r_deixado">

    <label for="dados_fase_a">Dados de Placa Fase A</label>
    <input type="text" id="dados_fase_a" name="dados_fase_a">

    <label for="dados_fase_b">Dados de Placa Fase B</label>
    <input type="text" id="dados_fase_b" name="dados_fase_b">

    <label for="dados_fase_v">Dados de Placa Fase V</label>
    <input type="text" id="dados_fase_v" name="dados_fase_v">

    <label for="dados_fase_r">Dados de Placa Fase R</label>
    <input type="text" id="dados_fase_r" name="dados_fase_r">

    <label for="condicao_pintura_a">Condição da Pintura Fase A</label>
    <input type="text" id="condicao_pintura_a" name="condicao_pintura_a">

    <label for="condicao_pintura_b">Condição da Pintura Fase B</label>
    <input type="text" id="condicao_pintura_b" name="condicao_pintura_b">

    <label for="condicao_pintura_v">Condição da Pintura Fase V</label>
    <input type="text" id="condicao_pintura_v" name="condicao_pintura_v">

    <label for="condicao_pintura_r">Condição da Pintura Fase R</label>
    <input type="text" id="condicao_pintura_r" name="condicao_pintura_r">

    <label for="fixacao_a">Fixação/Montagem Fase A</label>
    <input type="text" id="fixacao_a" name="fixacao_a">

    <label for="fixacao_b">Fixação/Montagem Fase B</label>
    <input type="text" id="fixacao_b" name="fixacao_b">

    <label for="fixacao_v">Fixação/Montagem Fase V</label>
    <input type="text" id="fixacao_v" name="fixacao_v">

    <label for="fixacao_r">Fixação/Montagem Fase R</label>
    <input type="text" id="fixacao_r" name="fixacao_r">

    <label for="aterramento_a">Aterramento Fase A</label>
    <input type="text" id="aterramento_a" name="aterramento_a">

    <label for="aterramento_b">Aterramento Fase B</label>
    <input type="text" id="aterramento_b" name="aterramento_b">

    <label for="aterramento_v">Aterramento Fase V</label>
    <input type="text" id="aterramento_v" name="aterramento_v">

    <label for="aterramento_r">Aterramento Fase R</label>
    <input type="text" id="aterramento_r" name="aterramento_r">

    <label for="cabos_condicoes_a">Condições dos Cabos e Conexões Fase A</label>
    <input type="text" id="cabos_condicoes_a" name="cabos_condicoes_a">

    <label for="cabos_condicoes_b">Condições dos Cabos e Conexões Fase B</label>
    <input type="text" id="cabos_condicoes_b" name="cabos_condicoes_b">

    <label for="cabos_condicoes_v">Condições dos Cabos e Conexões Fase V</label>
    <input type="text" id="cabos_condicoes_v" name="cabos_condicoes_v">

    <label for="cabos_condicoes_r">Condições dos Cabos e Conexões Fase R</label>
    <input type="text" id="cabos_condicoes_r" name="cabos_condicoes_r">

    <label for="re_aperto_a">Re-aperto dos Terminais Fase A</label>
    <input type="text" id="re_aperto_a" name="re_aperto_a">

    <label for="re_aperto_b">Re-aperto dos Terminais Fase B</label>
    <input type="text" id="re_aperto_b" name="re_aperto_b">

    <label for="re_aperto_v">Re-aperto dos Terminais Fase V</label>
    <input type="text" id="re_aperto_v" name="re_aperto_v">

    <label for="re_aperto_r">Re-aperto dos Terminais Fase R</label>
    <input type="text" id="re_aperto_r" name="re_aperto_r">

    <label for="isoladores_a">Isoladores Fase A</label>
    <input type="text" id="isoladores_a" name="isoladores_a">

    <label for="isoladores_b">Isoladores Fase B</label>
    <input type="text" id="isoladores_b" name="isoladores_b">

    <label for="isoladores_v">Isoladores Fase V</label>
    <input type="text" id="isoladores_v" name="isoladores_v">

    <label for="isoladores_r">Isoladores Fase R</label>
    <input type="text" id="isoladores_r" name="isoladores_r">

    <button type="submit">Enviar</button>
</form>

</body>
</html>
```
